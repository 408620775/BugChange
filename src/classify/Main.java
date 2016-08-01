package classify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Main {

	public static void main(String[] args) throws Exception {
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(new File("MyJedit.arff"));
		Instances instances = arffLoader.getDataSet();
		PreProcess preProcess = new PreProcess();
		instances = preProcess.NumLn(instances, "bug_introducing");
		// ClassifyCalculate classifyCalculate = new
		// ClassifyCalculate(instances,
		// "bug_introducing");
		// classifyCalculate.totalCal();
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(instances);
		arffSaver.setFile(new File("MyJeditFilter.arff"));
		arffSaver.writeBatch();
	}

}
