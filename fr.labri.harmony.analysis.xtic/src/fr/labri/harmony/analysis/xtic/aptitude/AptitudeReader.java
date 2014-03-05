package fr.labri.harmony.analysis.xtic.aptitude;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import fr.labri.harmony.analysis.xtic.aptitude.filter.ContentFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.FileFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.KindFilter;
import fr.labri.harmony.analysis.xtic.aptitude.filter.TreeFilter;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.log.HarmonyLogger;
import fr.labri.harmony.core.output.FileUtils;

public class AptitudeReader {
	public static List<Aptitude> readXTicConfig(AnalysisConfiguration config) throws AptitudeReaderException {
		if(config.getOptions()==null || config.getOptions().get("xtic-files")==null) {
			HarmonyLogger.error("The configuration file does not an 'xtic-files' option");
			HarmonyLogger.error("Default Demo file is considered");
			FileUtils.copyFile("fr.labri.harmony.analysis.xtic", "xtic/default.xml", Paths.get("tmp/default.xml"));
			return readXticFile(new File("tmp/default.xml").getPath());
		}
		File xticsource = new File(config.getOptions().get("xtic-files").toString());
		if (!xticsource.exists())
			throw new AptitudeReaderException("Directory " + xticsource + " does not exist");

		List<Aptitude> configs = new ArrayList<>();
		List<String> idName = new ArrayList<String>();
		for (File file : xticsource.listFiles()) {
			if (file.getName().endsWith(".xml")) {
				if (isValid(file.getAbsolutePath()))
					configs.addAll(readXticFile(file.getAbsoluteFile().getAbsolutePath()));
				else
					throw new AptitudeReaderException("XML File " + file.getAbsolutePath() + " is not well formed");
			}
		}
		for(Aptitude apt : configs) {
			if(idName.contains(apt.getIdName())){
				throw new AptitudeReaderException("Problem : Two Aptitude files have the same id");
			}
			else 
				idName.add(apt.getIdName());
		}
		return configs;
	}

	public static boolean isValid(String xmlFile) {
		try {
			String xsd_schema = FileUtils.getFileContent("fr.labri.harmony.analysis.xtic", "xtic/schema.xsd", null);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(new StringReader(xsd_schema)));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(Charset.forName("UTF-8").decode(ByteBuffer.wrap(Files.readAllBytes(Paths.get(xmlFile)))).toString())));
			return true;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;

	}

	public static List<Aptitude> readXticFile(String file) throws AptitudeReaderException {

		List<Aptitude> configs = new ArrayList<>();
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(file);

		try {
			Document document = builder.build(xmlFile);
			Element rootNode = document.getRootElement();

			List<Element> listAptitudes = rootNode.getChildren("aptitude");
			//For each aptitude
			for(Element aptitude : listAptitudes) {
				String idApt = aptitude.getAttributeValue("id");
				String descApt = "";
				if (aptitude.getAttributeValue("desc") != null) {
					descApt = aptitude.getAttributeValue("desc");
				}

				List<Element> listPatterns = aptitude.getChildren("pattern");
				List<PatternAptitude> patterns = new ArrayList<PatternAptitude>();

				for (int j = 0; j < listPatterns.size(); j++) {
					Element pattern = (Element) listPatterns.get(j);

					String id = pattern.getAttributeValue("id");

					String desc = "";
					if (pattern.getAttributeValue("desc") != null) {
						desc = pattern.getAttributeValue("desc");
					}
					
					Parser p = null;
					KindFilter kind = null;
					List<FileFilter> files = new ArrayList<FileFilter>();
					List<ContentFilter> contents = new ArrayList<ContentFilter>();
					List<TreeFilter> queries = new ArrayList<>();
	
					
					if (pattern.getChild("kind") != null) {
						String value = pattern.getChild("kind").getAttributeValue("value");
						kind = new KindFilter(value, true, "target");
					}
					else 
						kind = new KindFilter("*", true, "target");
					//Files
					if (pattern.getChild("files") != null) {
						Element fileElts = pattern.getChild("files");
						for(Element e : fileElts.getChildren("file")) {
							String value = e.getAttributeValue("value");
							boolean presence = true;
							String dir = "target";
							if(e.getAttributeValue("direction")!=null){
								dir = e.getAttributeValue("direction");
							}
							if(e.getAttributeValue("presence")!=null){
								presence =  Boolean.parseBoolean(e.getAttributeValue("presence"));
							}
							files.add(new FileFilter(value, presence, dir));
						}
					}
				
					//Contents
					if (pattern.getChild("contents") != null) {
						Element contentElts = pattern.getChild("contents");
						for (Element e : contentElts.getChildren("content")) {
							boolean presence = Aptitude.CONTENT_PRESENCE_DEFAULT;
							String dir = Aptitude.CONTENT_DIRECTION_DEFAULT;
							if (e.getAttribute("presence") != null && e.getAttributeValue("presence").equals("false"))
								presence=false;
							if (e.getAttribute("direction") != null)
								dir = e.getAttributeValue("direction");
							contents.add(new ContentFilter(e.getAttribute("value").getValue().trim(), presence, dir));
						}
					}
					//Diff
					if (pattern.getChild("diff") != null) {
						Element diff = pattern.getChild("diff");
						if (diff.getAttribute("parser") == null) {
							throw new AptitudeReaderException("Parser attribut for <diff> element cannot be empty");
						}
						Map<String,String> options = new HashMap<String, String>();
						if(diff.getChild("options")!=null) {
							for(Element option : diff.getChild("options").getChildren("option"))
								options.put(option.getAttributeValue("key"), option.getAttributeValue("value"));
						}
						String parserValue = diff.getAttribute("parser").getValue();
						p = Parser.buildParser(parserValue);

						if (p == null) {
							throw new AptitudeReaderException("Parser " + parserValue + " is not recognized");
						}
						p.setOptions(options);
						
						
						Element queriesElt = diff.getChild("xpath");
						for (Element e : queriesElt.getChildren("query")) {
							String direction="target";
							boolean presence=true;
							if(e.getAttributeValue("direction")!=null){
								direction = e.getAttributeValue("direction");
							}
							if(e.getAttributeValue("presence")!=null){
								presence =  Boolean.parseBoolean(e.getAttributeValue("presence"));
							}
							String expr = e.getAttribute("value").getValue().replaceAll("\n", "");
							queries.add(new TreeFilter(expr, presence, direction));
						}
						
					}
					patterns.add(new PatternAptitude(id, desc, kind, files, contents, p, queries));

				}
				Aptitude ac = new Aptitude(idApt, descApt, patterns);
				configs.add(ac);
				//			if (rootNode.getChild("formula") != null) {
				//				ac.setFormula(null);
				//			}
			}
			return configs;
		} catch (IOException io) {
			System.out.println(io.getMessage());
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		}
		return null;
	}
}