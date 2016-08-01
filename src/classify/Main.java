package classify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Main {

	public static void main(String[] args) throws Exception {
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(new File("MyAnt.arff"));
		Instances instances = arffLoader.getDataSet();
		ClassifyCalculate classifyCalculate = new ClassifyCalculate(instances,
				"bug_introducing");
		classifyCalculate.totalCal();
	}

}
