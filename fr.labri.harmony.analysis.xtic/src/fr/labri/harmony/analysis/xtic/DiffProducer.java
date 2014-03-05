package fr.labri.harmony.analysis.xtic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.labri.gumtree.algo.LcsMatcherOptimize;
import fr.labri.gumtree.gen.jdt.JdtTreeGenerator;
import fr.labri.gumtree.gen.js.RhinoTreeGenerator;
import fr.labri.gumtree.gen.xml.XMLTreeGenerator;
import fr.labri.gumtree.tree.MappingIoUtils;
import fr.labri.gumtree.tree.Tree;
import fr.labri.gumtree.tree.XTicTreeGenerator;
import fr.labri.harmony.analysis.xtic.aptitude.Parser;

public abstract class DiffProducer {

	public abstract String computeDiffToXml(File oldFile, File newFile, boolean targetNewFile);

	private static Map<String,String> options = new HashMap<String, String>();

	public static DiffProducer Factory(Parser p) {
		switch (p) {
		case JAVA:
			options = p.getOptions();
			return diffJAVA;
		case JS:
			options = p.getOptions();
			return diffJS;
		case XML:
			options = p.getOptions();
			return diffXML;
		}
		return null;
	}

	final static DiffProducer diffJS = new DiffTreeProducer () {
		@Override
		public String computeDiffToXml(File oldFile, File newFile, boolean targetNewFile) {
			return computeTree(oldFile, newFile, targetNewFile, new RhinoTreeGenerator());
		}
	};

	final static DiffProducer diffXML = new DiffTreeProducer() {
		@Override
		public String computeDiffToXml(File oldFile, File newFile, boolean targetNewFile) {
			return computeTree(oldFile, newFile, targetNewFile, new XMLTreeGenerator());
		}
	};

	final static DiffProducer diffJAVA = new  DiffTreeProducer() {
		@Override
		public String computeDiffToXml(File oldFile, File newFile, boolean targetNewFile) {
			return computeTree(oldFile, newFile, targetNewFile, new JdtTreeGenerator());
		}
	};

	private static String computeTree(File oldFile, File newFile, boolean targetNewFile, XTicTreeGenerator generator) {
		try {
			
			String xml = "";
			if (oldFile == null) {
				Tree dst = generator.generate(newFile.toString(), options);
				if (dst == null)
					return "";
				xml = MappingIoUtils.writeXML(dst, null, true);
			} else {
				Tree src = generator.generate(oldFile.toString(), options);
				if (src == null)
					return "";
				Tree dst = generator.generate(newFile.toString(), options);
				if (dst == null)
					return "";
				if(targetNewFile) {
					LcsMatcherOptimize matcher = new LcsMatcherOptimize(src, dst);
					xml = MappingIoUtils.writeXML(dst, matcher.getMappings(), true);
				}
				else {
					LcsMatcherOptimize matcher = new LcsMatcherOptimize(dst, src);
					xml = MappingIoUtils.writeXML(src, matcher.getMappings(), false);
				}
			}
	
			return xml;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static abstract class DiffTreeProducer extends DiffProducer {
		/*
		 * Delegates to the GumTree project
		 * see https://code.google.com/p/labri-se/source/checkout?repo=gumtree
		 */
	}
}
