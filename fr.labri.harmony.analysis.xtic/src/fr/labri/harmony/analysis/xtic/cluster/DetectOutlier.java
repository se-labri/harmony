package fr.labri.harmony.analysis.xtic.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//http://www.wikihow.com/Calculate-Outliers
public class DetectOutlier {
	
	public static void main(String[] args) {
		List<Integer> numbers = new ArrayList<Integer>();
		for(int i = 0 ;  i < 50 ; i++) {
			numbers.add(new Random().nextInt(50));
		}
		numbers.add(134);
		numbers.add(99);
		numbers.add(128);
		//1 Sort Data
		Collections.sort(numbers);
		//computation of the lowest quartile, point below which 25% of the numbers are
		double lowq = 0;
		lowq = numbers.get((numbers.size()/4)-1);
		double highq = 0;
		highq = numbers.get(((numbers.size()/4)*3)-1);
		//find the "innerfences"
		//1.  (highq - lowq) -> the interquartile range
		// v = (highq - lowq) * 1.5
		// 1.5 = mild outlier
		// inner fences = highq + v  et lowq -v
		
		double upper_infer_fence = highq + (highq-lowq)*1.5;
		double extreme_upper_infer_fence = highq + (highq-lowq)*3.0;
		//any point after innerfence1 is an outlier
		//repeat the same operation but multiply by 3 to detect extreme outlier;
		System.out.println(lowq+" "+highq+" "+upper_infer_fence);
		System.out.println("mild");
		for(int number : numbers)
			if((double)number > upper_infer_fence)
				System.out.println(number);
		System.out.println("extreme");
		for(int number : numbers)
			if((double)number > extreme_upper_infer_fence)
				System.out.println(number);
	}
	
}
