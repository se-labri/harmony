package fr.labri.harmony.analysis.xtic;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xerial.snappy.Snappy;

import fr.labri.Timer.TimerToken;
import fr.labri.harmony.analysis.xtic.XticAnalysis.AnalyseSource;
import fr.labri.harmony.analysis.xtic.aptitude.PatternAptitude;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.model.Action;
import fr.labri.harmony.core.model.ActionKind;
import fr.labri.harmony.core.model.Event;
import fr.labri.harmony.core.model.Source;

public class FilterVCS implements FilterXTic {

	static boolean toCompute = true;
	static final double NCD_MIN = 0.00D;
	static final double NCD_MAX = 0.25D;
	List<PatternAptitude> _patterns = new ArrayList<PatternAptitude>();

	public FilterVCS(List<PatternAptitude> patterns) {
		_patterns = patterns;
	}

	public Object filter(AnalyseSource analyse, Event event, Event parent, List<Action> actions) {

		if(toCompute==false)
			return new HashMap<Action,Action>();

		Source src = analyse._src;

		boolean deleteContent = false;
		boolean addedContent = false;
		for (Action a : actions) {
			if (a.getKind().equals(ActionKind.Delete))
				deleteContent = true;
			if (a.getKind().equals(ActionKind.Create))
				addedContent = true;
			if (addedContent && deleteContent)
				break;
		}

		String localpath = src.getWorkspace().getPath() + "/";

		if (deleteContent && addedContent) {
			Map<File, Action> itemIds = new HashMap<File, Action>();
			Set<File> deleted = new HashSet<>();
			for (Action a : actions) {
				if (a.getKind().equals(ActionKind.Delete)){
					if (new File(localpath + a.getItem().getNativeId()).isDirectory())
						continue;
					for(PatternAptitude pattern : _patterns) {
						if(pattern.acceptFile(a)) {
							File p = analyse.checkoutFile(parent, a, "v0", "checkout_vcs");
							deleted.add(p);
							itemIds.put(p, a); 
							break;
						}
					}
				}
			}
			Set<File> created = new HashSet<>();
			for (Action a : actions) {
				if (a.getKind().equals(ActionKind.Create)) {
					if (new File(localpath + a.getItem().getNativeId()).isDirectory())
						continue;
					for(PatternAptitude pattern : _patterns) {
						if(pattern.acceptFile(a)) {
							File p = analyse.checkoutFile(event, a, "v1", "checkout_vcs");
							created.add(p);
							itemIds.put(p, a);
							break;
						}
					}
				}
			}

			TimerToken ncd = analyse._timer.start("ncd");
			Map<Action,Action> renamed = bestNCD(actions, itemIds, created, deleted);
			ncd.stop();
			if (renamed.size() > 0)
				HarmonyLogger.info(renamed.size()+" files detected as renamed/moved" + "\t" + event.getNativeId());
			return renamed;
		}
		return null;
	}

	Map<Action,Action> bestNCD(List<Action> actions, Map<File, Action> itemIds, Set<File> created, Set<File> deleted) {
		// Mappings between deleted et created pour choper les move et rename

		Map<Action,Action> renamed = new HashMap<Action, Action>();
		Map<File, File> best_cdt = computeNCDScore(created, deleted);
		for (File p : deleted) {
			if (best_cdt.containsKey(p)) {
				if (p.equals(best_cdt.get(best_cdt.get(p)))) {
					for (Action a : actions) {
						if (a.getItem().getNativeId().equals(itemIds.get(best_cdt.get(p)).getItem().getNativeId())) {
							renamed.put(a,itemIds.get(p));
							break;
						}
					}
				}
			}
		}
		return renamed;
	}

	private Map<File, File> computeNCDScore(Set<File> created, Set<File> deleted) {
		Map<File, File> best_cdt = new HashMap<File, File>();
		Map<File, Double> best_score = new HashMap<File, Double>();
		Map<File, String> index_content = new HashMap<File, String>();
		Map<File, Integer> index_score = new HashMap<File, Integer>();
		for (File del : deleted) {
			for (File cre : created) {
				double value = NCDUtils.computeNCD(del, cre, index_content, index_score);
				if (value >= NCD_MIN && value <= NCD_MAX) {
					if (best_cdt.containsKey(del)) {
						if ((best_score.get(del) > value)) {
							best_cdt.put(del, cre);
							best_score.put(del, value);
						}
					} else {
						best_cdt.put(del, cre);
						best_score.put(del, value);
					}
					if (best_cdt.containsKey(cre)) {
						if ((best_score.get(cre) > value)) {
							best_cdt.put(cre, del);
							best_score.put(cre, value);
						}
					} else {
						best_cdt.put(cre, del);
						best_score.put(cre, value);
					}
				}
			}
		}
		index_content.clear();
		return best_cdt;
	}
}


class NCDUtils {

	static public Double computeNCD(File source, File target, Map<File, String> index_content, Map<File, Integer> index_score) {
		if (source.toString().lastIndexOf(".") == -1)
			return 1.0;
		if (target.toString().lastIndexOf(".") == -1)
			return 1.0;
		String ext1 = source.toString().substring(source.toString().lastIndexOf("."));
		String ext2 = target.toString().substring(target.toString().lastIndexOf("."));
		if (ext1.equals(ext2) == false) {
			return 1.0;
		}

		String x, y;
		int cx, cy;
		try {
			if (index_content.containsKey(source)) {
				x = index_content.get(source);
				cx = index_score.get(source);
			} else {
				x = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(source.getAbsolutePath())))).toString();
				index_content.put(source, x);
				cx = C(x);
				index_score.put(source, cx);
			}
			if (index_content.containsKey(target)) {
				y = index_content.get(target);
				cy = index_score.get(target);
			} else {
				y = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(target.getAbsolutePath())))).toString();
				index_content.put(target, y);
				cy = C(y);
				index_score.put(target, cy);
			}
			int cxy = C(x + y);
			return (cxy - (double) Math.min(cx, cy)) / Math.max(cx, cy);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1.0;
	}

	static private int C(String inputString) {
		try {
			byte[] compressed = Snappy.compress(inputString);
			return compressed.length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static void main(String[] args) {
		File source = new File("/home/cedric/gitTest/gitTest/Test/src/labri/harmony/Foo.java");
		File target = new File("/home/cedric/gitTest/gitTest/Test/src/labri/harmony/xtic/Foo.java");
		String x, y;
		int cx, cy;
		try {
			x = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(source.getAbsolutePath())))).toString();
			y = Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(target.getAbsolutePath())))).toString();
			cy = C(y);
			cx = C(x);	
			int cxy = C(x + y);
			double score = (cxy - (double) Math.min(cx, cy)) / Math.max(cx, cy);
			System.out.println(score);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}




