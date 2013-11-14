package fr.labri.harmony.analysis.xtic.aptitude;

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

public class Query {


	private XPathExpression _exprSource;
	private XPathExpression _exprTarget;
	private XPathExpression _exprBoth;

	public XPathExpression get_exprSource() {
		return _exprSource;
	}


	public void set_exprSource(XPathExpression _exprSource) {
		this._exprSource = _exprSource;
	}


	public XPathExpression get_exprTarget() {
		return _exprTarget;
	}


	public void set_exprTarget(XPathExpression _exprTarget) {
		this._exprTarget = _exprTarget;
	}

	public Query(String querySource, String queryTarget, String queryBoth) {
		try {
			System.setProperty("javax.xml.xpath.XPathFactory:http://saxon.sf.net/", "net.sf.saxon.xpath.XPathFactoryImpl");
			XPathFactory xpf = XPathFactory.newInstance("http://saxon.sf.net/");
			XPath xpath = xpf.newXPath();
			if(!querySource.isEmpty())
				_exprSource = xpath.compile(querySource);
			if(!queryTarget.isEmpty())
				_exprTarget = xpath.compile(queryTarget);
			if(!queryBoth.isEmpty())
				_exprBoth = xpath.compile(queryBoth);
		} catch (XPathFactoryConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}


	public int xpath(String oldDiff, String newDiff) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			int oldResult = 0;
			if(_exprSource != null ) {
				if(!oldDiff.isEmpty()){
					Document doc = db.parse(new InputSource(new StringReader(oldDiff)));
					oldResult = ((NodeList) _exprSource.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(oldResult==0)
						return 0;
				}
				else return 0;
			}
			int newResult = 0;
			if(_exprTarget != null) {
				if(!newDiff.isEmpty()){
					Document doc = db.parse(new InputSource(new StringReader(newDiff)));
					newResult = ((NodeList) _exprTarget.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(newResult==0)
						return 0;
				}
				else return 0;
			}
			if(_exprBoth != null) {
				if(!oldDiff.isEmpty() && !newDiff.isEmpty()) {
					Document doc = db.parse(new InputSource(new StringReader(oldDiff)));
					oldResult = ((NodeList) _exprBoth.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(oldResult==0)
						return 0;
					doc = db.parse(new InputSource(new StringReader(newDiff)));
					newResult = ((NodeList) _exprBoth.evaluate(doc, XPathConstants.NODESET)).getLength();
					if(newResult==0)
						return 0;
				}
				else return 0;
			}
			//If there is a combination of queries
			if( (_exprSource!=null && _exprTarget!=null && _exprBoth!=null) ||
					(_exprSource!=null && _exprTarget!=null) ||
					(_exprSource!=null && _exprBoth!=null) ||
					(_exprTarget!=null && _exprBoth!=null) ||
					(_exprBoth!=null)) {
				return 1;
			}
			//if there is only "both", we return 1.
			//If there is only source or target, we return the number of node founds.
			else if(_exprSource!=null)
				return oldResult;
			else if(_exprTarget!=null) 
				return newResult;
		} catch (ParserConfigurationException | IOException | XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			
		}
		return 0;
	}


	public XPathExpression get_exprBoth() {
		return _exprBoth;
	}


	public void set_exprBoth(XPathExpression _exprBoth) {
		this._exprBoth = _exprBoth;
	}
}