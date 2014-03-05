package fr.labri.harmony.analysis.xtic.aptitude;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class Validation {
	public static void main(String[] args) throws Exception {

		try
		{
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(new FileInputStream(new File("/home/cedric/Documents/Projets/xtic/bitbucket/xtic-draft/fr.labri.harmony.analysis.xtic/xtic/schema.xsd"))));
			for(File file : new File("/home/cedric/Harmony/HarmonyWorkingDirectory/xtic").listFiles()) {
				System.out.println(file.getAbsolutePath());
				if(file.getName().endsWith(".xml")) {
					System.out.println(file.getName());
					Validator validator = schema.newValidator();
					validator.validate(new StreamSource(new FileInputStream(file)));
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
