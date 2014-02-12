package fr.labri.harmony.analysis.cloc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fr.labri.harmony.core.source.WorkspaceException;

public class ClocRunner {
	
	public static ClocEntries runCloc(String workspacePath) {
		ClocEntries entries = null;
		try {			
			Process p = Runtime.getRuntime().exec(new String[] { "cloc", new File(workspacePath).getAbsolutePath(), "--xml", "--quiet" });
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer b = new StringBuffer();
			String line;
			while ((line = r.readLine()) != null) {
				if (line.trim().startsWith("<")) b.append(line + "\n");
			}
			p.waitFor();
			r.close();

			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = df.newDocumentBuilder();

			Document doc = db.parse(new InputSource(new StringReader(b.toString().trim())));
			NodeList l = doc.getElementsByTagName("language");

			entries = new ClocEntries();
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

		} catch (Exception ex) {
			throw new WorkspaceException(ex);
		}
		return entries;
	}

}
