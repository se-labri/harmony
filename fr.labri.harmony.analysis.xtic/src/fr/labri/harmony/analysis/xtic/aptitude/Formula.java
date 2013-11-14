package fr.labri.harmony.analysis.xtic.aptitude;

import java.util.Collection;


public interface Formula {
	double apply(Collection<Double> data);
}

class DefaultFormula implements Formula {

	@Override
	public double apply(Collection<Double> data) {
		Double d = 0.0D;
		for(double value : data) {
			d += value;
		}
		return d;
	}
}

//class ScriptFormula implements Formula {
//	static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//
//	@Override
//	public double apply(Collection<Double> data) {
//		for(Aptitude sc : map.keySet()) {
//			if(map.get(sc) == null) {
//				f = f.replaceAll(sc.getIdName(), "0");
//			} else {
//				f = f.replaceAll(sc.getIdName(), Integer.toString(map.get(sc).size()));
//			}
//		}
//		try {
//			return Double.valueOf(engine.eval(f).toString());
//		} catch (NumberFormatException | ScriptException e) {
//			e.printStackTrace();
//		}
//	}
//}
