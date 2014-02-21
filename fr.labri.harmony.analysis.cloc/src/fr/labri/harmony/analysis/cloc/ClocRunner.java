package fr.labri.harmony.analysis.cloc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.labri.harmony.core.source.WorkspaceException;

public class ClocRunner {

	public static ClocEntries runCloc(String workspacePath) {
		ClocEntries entries = new ClocEntries();
		Process p;
		try {
			p = Runtime.getRuntime().exec(new String[] { "cloc", "--xml", "--quiet", "--skip-uniqueness", new File(workspacePath).getAbsolutePath() });
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer b = new StringBuffer();
			String line;
			while ((line = r.readLine()) != null) {
				if (line.trim().startsWith("<")) b.append(line + "\n");
			}
			p.waitFor();
			r.close();

			// If the length is null, there was no output, probably because there are only text files.
			if (b.length() != 0) {
				DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = df.newDocumentBuilder();

				Document doc = db.parse(new InputSource(new StringReader(b.toString().trim())));
				NodeList l = doc.getElementsByTagName("language");

				for (int i = 0; i < l.getLength(); i++) {
					Node el = l.item(i);
					final NamedNodeMap attributes = el.getAttributes();
					String lang = attributes.getNamedItem("name").getTextContent();
					ClocEntry e = new ClocEntry();
					e.setLanguage(lang);
					e.setCode(Integer.parseInt(attributes.getNamedItem("code").getTextContent()));
					e.setFile(Integer.parseInt(attributes.getNamedItem("files_count").getTextContent()));
					e.setBlank(Integer.parseInt(attributes.getNamedItem("blank").getTextContent()));
					e.setComment(Integer.parseInt(attributes.getNamedItem("comment").getTextContent()));
					entries.getEntries().add(e);
				}
			}
		} catch (IOException | InterruptedException | SAXException | ParserConfigurationException e) {
			throw new WorkspaceException(e);
		}

		return entries;
	}

}
