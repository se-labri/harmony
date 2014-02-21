package fr.labri.harmony.analysis.xtic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import fr.labri.Counters;
import fr.labri.Timer;
import fr.labri.Timer.TimerToken;
import fr.labri.harmony.analysis.xtic.aptitude.Aptitude;
import fr.labri.harmony.analysis.xtic.aptitude.AptitudeReader;
import fr.labri.harmony.analysis.xtic.aptitude.AptitudeReaderException;
import fr.labri.harmony.analysis.xtic.aptitude.Parser;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class XticAnalysis extends AbstractAnalysis {

	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("xtic.debug", "false"));
	private static final boolean TIMER = Boolean.parseBoolean(System.getProperty("xtic.timer", "false"));
	private static final boolean BENCHMARK = Boolean.parseBoolean(System.getProperty("xtic.benchmark", "false"));
	private static final int BENCHMARK_RUN = Integer.parseInt(System.getProperty("xtic.benchmark.run", "1"));

	public XticAnalysis() {
		super();
	}

	public XticAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
		if(config.getOptions() != null ) {
			if(config.getOptions().containsKey("MAX_COMMIT_SIZE")) {
				FilterTooManyChanges.LIMIT_ACTIONS_PER_EVENT = Integer.valueOf(config.getOptions().get("MAX_COMMIT_SIZE").toString());
			}
			if(config.getOptions().containsKey("RENAME_MOVE")) {
				FilterVCS.toCompute = config.getOptions().get("RENAME_MOVE").toString().toUpperCase().equals("FALSE") ? false : true;
			}
		}
	}

	@Override
	public void runOn(Source src) {
		List<Aptitude> aptitudes = new ArrayList<>();
		Map<Aptitude, List<PatternAptitude>> patterns = new LinkedHashMap<>();
		try {
			aptitudes = AptitudeReader.readXTicConfig(config);
			List<PatternAptitude> apt = new ArrayList<>();
			if (!BENCHMARK)
				patterns.put(null, apt);
			else
				patterns.put(new Aptitude("harmony_model","",new ArrayList<PatternAptitude>()), new ArrayList<PatternAptitude>());

			for (Aptitude aptitude : aptitudes) {
				if (BENCHMARK)
					apt = new ArrayList<PatternAptitude>();
				HarmonyLogger.info("Loaded Aptitudes : " + aptitude.getIdName());
				aptitude.addPatterns(apt);
				if (BENCHMARK) {
					for (int i = 0; i < BENCHMARK_RUN; i++) {
						patterns.put(new Aptitude(aptitude.getIdName() + "-" + i, ""), apt);
					}
				}
			}
		} catch (AptitudeReaderException e) {
			e.printStackTrace();
			return;
		}

		for (Aptitude as : aptitudes) {
			dao.saveData(this.getPersitenceUnitName(), as, src);
		}

		if (BENCHMARK)
			for (Aptitude apt : patterns.keySet())
				doAnalyse(src, apt, patterns);
		else
			doAnalyse(src, null, patterns);
	}

	private void doAnalyse(Source src, Aptitude as, Map<Aptitude, List<PatternAptitude>> patterns) {
		try { // TODO remove this try catch after harmony update
			AnalyseSource analyse = new AnalyseSource(src, as, patterns);
			analyse.perform();
			if(TIMER)
				analyse.dumpResults();
			analyse.daoPersistResults();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class AnalyseSource {
		final Source _src;
		final Aptitude _apt;
		final List<PatternAptitude> _patterns;
		final Map<String, Developer> _indexDevs = new HashMap<String, Developer>();

		final Counters<String> _actions = new Counters<>();
		final Timer<String> _timer = new Timer<>(Timer.simpleFactory());
		final FilterXTic _mimeFilter;

		public AnalyseSource(Source src, Aptitude aptitude, Map<Aptitude, List<PatternAptitude>> patterns) throws IOException {
			this._src = src;
			_apt = aptitude;
			_patterns = patterns.get(BENCHMARK ? _apt : null);
			_mimeFilter = FilterMimeType.getFilter(_patterns);
		}

		public Timer<String> getTimer() {
			return _timer;
		}

		public void perform() {
			if (DEBUG)
				System.out.printf("Start on %s", _src.getUrl());

			TimerToken all = null;
			TimerToken init = null;

			if(TIMER)
				all = _timer.start("all");
			if(TIMER)
				init = _timer.start("harmony_get_data_from_model");

			List<Event> oneBranch = _src.getEvents();
			Collections.sort(oneBranch, new EventComparator());
			if(TIMER)
				init.stop();

			int i = 0;
			HarmonyLogger.info(oneBranch.size() + " events to compute ["+_src.getUrl()+"]");
			for (Event e : oneBranch) {
				if (++i % 50 == 0) {
					SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
					HarmonyLogger.info("[" + df.format(new Date(System.currentTimeMillis())) + "] " + i + " events computed");
					System.gc();
				}
				TimerToken harmony = null;
				if(TIMER) 
					harmony = _timer.start("new_event");
				computeNewEvent(e);
				if(TIMER) 
					harmony.stop();
			}
			if(TIMER)
				all.stop();
		}

		public Developer getDeveloper(Event e) {
			Author auth = e.getAuthors().get(0);
			String name = auth.getName();
			Developer dev = _indexDevs.get(name);
			if (dev == null) {
				String mail = auth.getEmail();
				dev = new Developer(name);
				dev.setEmail(mail);
				_indexDevs.put(name, dev);
			}
			dev.incrCommit();
			return dev;
		}

		public Object applyPreprocessFilter(String key, FilterXTic filter, Event event, Event parent, List<Action> actions) {
			if (actions.isEmpty())
				return null;
			TimerToken filterTime = null;
			if(TIMER)
				filterTime = _timer.start(key);
			int size = actions.size();
			Object o = filter.filter(this, event, parent, actions);
			if(TIMER)
				filterTime.stop();
			_actions.add(key, size - actions.size());
			return o;
		}

		@SuppressWarnings("unchecked")
		public void computeNewEvent(Event e) {
			TimerToken harmony = null;
			if(TIMER)
				harmony = _timer.start("harmony_get_data_from_model");
			Developer dev = getDeveloper(e);
			List<Event> parents = e.getParents();
			if(TIMER)
				harmony.stop();

			for (Event parent : parents) {
				// Get actions list for the specific parent
				TimerToken par = null;
				if(TIMER)
					par = _timer.start("harmony_get_data_from_model");
				List<Action> actions = new ArrayList<Action>(e.getActions(parent));
				_actions.add("actions", actions.size());
				if(TIMER)
					par.stop();

				Map<Action,Action> renamedFiles = new HashMap<Action, Action>();
				TimerToken preprocess = null;
				if(TIMER)
					preprocess = _timer.start("preprocess");
				// Pre-processing
				applyPreprocessFilter("preprocess_toomany", new FilterTooManyChanges(), e, parent, actions);
				applyPreprocessFilter("preprocess_mime", _mimeFilter, e, parent, actions);
				Object res = applyPreprocessFilter("preprocess_fixvcs", new FilterVCS(), e, parent, actions);
				if(res!=null)
					renamedFiles = (Map<Action,Action>)res;
				applyPreprocessFilter("preprocess_fixvcs", new FilterDeleteAction(), e, parent, actions);
				_actions.add("actions_left", actions.size());
				if(TIMER)
					preprocess.stop();

				if (!actions.isEmpty()) {
					TimerToken aptitude = null;
					if(TIMER)
						aptitude = _timer.start("aptitude");

					for (Action a : actions) {
						Score score = new Score(a);
						for (PatternAptitude p : _patterns) {
							long s = score.compute(p,renamedFiles);
							dev.addAptitudePattern(p, e.getTimestamp(), s);
						}
						score = null;
					}
					if(TIMER)
						aptitude.stop();
				}
				actions = null;
			}
		}

		public File checkoutFile(Event event, Action action, String value) {
			return checkoutFile(event, action, value, "checkout");
		}

		public File checkoutFile(Event event, Action action, String value, String tag) {
			TimerToken checkout = null;
			if(TIMER)
				checkout = _timer.start(tag);
			_src.getWorkspace().update(event, action.getItem());
			File path = new File(_src.getWorkspace().getPath(), action.getItem().getNativeId());
			File newFile = new File(computeFile(path.toString(), value));
			newFile.delete();
			try {
				newFile.getParentFile().mkdirs();
				newFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			path.renameTo(newFile);
			if(TIMER)
				checkout.stop();
			return newFile;
		}

		private String computeFile(String srcFile, String value) {
			if (srcFile.contains(".")) {
				int p = srcFile.lastIndexOf(".");
				return srcFile.substring(0, p) + "_" + value + srcFile.substring(p, srcFile.length());
			} else
				return srcFile + "_" + value;
		}

		public void skillSummary(PrintStream out) {
			out.print("Name");
			for(PatternAptitude ap : _patterns)
				out.printf("; %s", ap.getIdName());
			out.println();
			for (Developer dev : _indexDevs.values()) {
				out.printf("\"%s\"", dev.getName());
				for(PatternAptitude ap : _patterns)
					out.printf(Locale.US, "; %d", ap.scoreFor(dev).getScore());
				out.println();
			}
			out.println();
		}


		public void timeSummary(PrintStream out) {
			_timer.dump(out);
		}

		public void countSummary(PrintStream out) {
			_actions.dump(out);
		}

		public void dumpResults() {
			if (DEBUG) {
				System.out.println("---------------------" + _apt);
				skillSummary(System.out);
				System.out.println("---------------------" + _apt);
				timeSummary(System.out);
				System.out.println("---------------------" + _apt);
				countSummary(System.out);
			}

			String fname = _src.getUrl().replaceAll("\\:", "\\_").replaceAll("\\/", "\\_") + (_apt == null ? "" : "_" + _apt.toString().replaceAll(" ", "_"));
			try {
				System.out.println("ECRITURE DU FICHIER TIME.CSV");
				FileOutputStream fw = new FileOutputStream(fname + "_time.csv");
				timeSummary(new PrintStream(fw));
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void daoPersistResults() {

			for (Developer dev : _indexDevs.values()) {				
				for(PatternAptitude apt : _patterns){
					dev.addAptitudeScore(apt, apt.scoreFor(dev));
				}
				dao.saveData(getPersitenceUnitName(), dev, _src);
			}
		}


		class Score {
			private Action _action;
			private Action _actionSource;
			private File _oldFile;
			private File _newFile;
			private String _text;
			private String _oldText;
			private String _xmlDiff[][] = new String[Parser.values().length][2];

			public Score(Action action) {
				_action = action;
			}

			private String getText(boolean newFile) {
				if(newFile) {
					if (_newFile == null || _text == null) {
						try {
							_newFile = checkoutFile(_action.getEvent(), _action, "v1");
							_text = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(_newFile.toString())))).toString();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					return _text;
				}
				else {
					if (_oldFile == null || _oldText == null) {
						try {
							_oldFile = checkoutFile(_action.getParentEvent(), _actionSource, "v0");
							_oldText = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(_oldFile.toString())))).toString();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					return _oldText;
				}
			}

			private String getDiff(Parser type, boolean targetNewFile) {
				int pos = type.ordinal();
				String res;
				if(targetNewFile)
					res = _xmlDiff[pos][1];
				else
					res = _xmlDiff[pos][0];
				if (res == null) {
					if (_newFile == null)
						_newFile = checkoutFile(_action.getEvent(), _action, "v1");
					if (_action.getKind().equals(ActionKind.Edit) || !_actionSource.equals(_action))
						_oldFile = checkoutFile(_action.getParentEvent(), _actionSource, "v0", "checkout_diff");

					TimerToken diff = null;
					if(TIMER)
						diff = _timer.start("aptitude_diff");
					
					_xmlDiff[pos][targetNewFile ? 1 : 0] = res = DiffProducer.Factory(type).computeDiffToXml(_oldFile, _newFile, targetNewFile);
					if(TIMER)
						diff.stop();
				}
				return res;
			}

			private long compute(PatternAptitude p, Map<Action, Action> renamedFiles) {
				if(renamedFiles.containsKey(_action))
					_actionSource = renamedFiles.get(_action);
				else
					_actionSource = _action;
				_actions.increment("in_pipe");
				p.getParser();
				if (!p.acceptFile(_action))
					return 0;
				_actions.increment("mime_ok");

				int toFind = p.getContents().size();

				if (toFind > 0) {
					String text = "", oldText = "";
					if(p.needContent(true))
						text = getText(true);
					if(p.needContent(false))
						oldText = getText(false);
					if(_text==null){
						return 0;
					}
					TimerToken patternmatching = null;
					if(TIMER)
						patternmatching = _timer.start("aptitude_matching");
					for (PatternContent pat : p.getContents()) {
						if (pat.checkPattern(oldText,text))
							toFind--;
					}
					if(TIMER)
						patternmatching.stop();
					if (toFind > 0)
						return 0;
				}
				_actions.increment("content_ok");
				if (p.getQueries()==null) {
					_actions.increment("nodiff_ok");
					return 1;
				}

				long result = 0;
				String xmlOldDiff = "", xmlNewDiff = "";
				if(p.needDiff(false))
					xmlOldDiff = getDiff(p.getParser(), false);
				if(p.needDiff(true))
					xmlNewDiff = getDiff(p.getParser(), true);

				if (!xmlNewDiff.isEmpty() || !xmlOldDiff.isEmpty()) {
					TimerToken xpath = null;
					if(TIMER)
						xpath = _timer.start("aptitude_xpath");
					result += p.getQueries().xpath(xmlOldDiff, xmlNewDiff);
					if(TIMER)
						xpath.stop();
				}
				if (result > 0)
					_actions.increment("diff_ok");

				return result;
			}
		}
	}
}


