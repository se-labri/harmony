package fr.labri.harmony.analysis.xtic.aptitude.filter;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TreeFilter extends Filter {
	
	private XPathExpression expression;

	public TreeFilter(String query, boolean presence, String direction) {
		super(presence, direction);
		try {
			System.setProperty("javax.xml.xpath.XPathFactory:http://saxon.sf.net/", "net.sf.saxon.xpath.XPathFactoryImpl");
			XPathFactory xpf = XPathFactory.newInstance("http://saxon.sf.net/");
			XPath xpath = xpf.newXPath();
			expression = xpath.compile(query);
		} catch (XPathFactoryConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}


	public int executeFilter(String oldElement, String newElement) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			int oldResult = 0;
			if(direction.equals("source")) {
				if(!oldElement.isEmpty()){
					Document doc = db.parse(new InputSource(new StringReader(oldElement)));
					oldResult = ((NodeList) expression.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(oldResult==0 && presence)
						return 0;
					if(oldResult > 0 && !presence)
						return 0;
				}
				else return 0;
			}
			int newResult = 0;
			if(direction.equals("target")) {
				if(!newElement.isEmpty()){
					Document doc = db.parse(new InputSource(new StringReader(newElement)));
					newResult = ((NodeList) expression.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(newResult==0 && presence)
						return 0;
					if(newResult > 0 && !presence)
						return 0;
				}
				else return 0;
			}
			if(direction.equals("both")) {
				if(!oldElement.isEmpty() && !newElement.isEmpty()) {
					//source
					Document doc = db.parse(new InputSource(new StringReader(oldElement)));
					oldResult = ((NodeList) expression.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(oldResult==0 && presence)
						return 0;
					if(oldResult > 0 && !presence)
						return 0;
					//target
					doc = db.parse(new InputSource(new StringReader(newElement)));
					newResult = ((NodeList) expression.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(newResult==0 && presence)
						return 0;
					if(newResult > 0 && !presence)
						return 0;
				}
				else return 0;
			}
			//If there is a combination of queries
			if(direction.equals("both"))
				return 1;
			else if(direction.equals("source"))
				return oldResult;
			else
				return newResult;
		} catch (ParserConfigurationException | IOException | XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			
		}
		return 0;
	}

	
}
