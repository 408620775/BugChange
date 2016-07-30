package classify;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Main {

	public static void main(String[] args) throws Exception {
      
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(new File("MyAnt.arff"));
		Instances instances=arffLoader.getDataSet();
		ClassifyCalculate classifyCalculate=new ClassifyCalculate(instances,"bug_introducing");
		classifyCalculate.totalCal();
		
	//PreProcess.csvToArff("eclipse.csv", "eclipse.arff");
		//PreProcess.rmAttribute("voldemort.arff");
	}

}
