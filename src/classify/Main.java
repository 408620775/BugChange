package classify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Main {

	public static void main(String[] args) throws Exception {
		File aFile = new File("V2Arff");
		String[] arffFiles = aFile.list();
		DecimalFormat df = new DecimalFormat("0.00");

		for (String string : arffFiles) {
			ArffLoader arffLoader = new ArffLoader();
			arffLoader.setFile(new File(aFile.getName()+"/"+string));
			Instances instances=arffLoader.getDataSet();
			Sample sample=new Sample("bug_introducing");
			Instances overInstances=sample.OverSample(instances);
			System.out.println(string);
			Classifier classifier=new J48();
			BaggingClassify2 baggingClassify2=new BaggingClassify2(classifier, overInstances, "bug_introducing");
			baggingClassify2.Evaluation();
			for (double value: baggingClassify2.getRes()) {
				System.out.print(value+"  ");
			}
			System.out.println();
		//	SimpleClassify simpleClassify=new SimpleClassify(classifier, overInstances,"bug_introducing");
		//	BaggingClassify simpleClassify=new BaggingClassify(classifier, instances, 2, "bug_introducing");
			BaggingClassify simpleClassify=new BaggingClassify(classifier, overInstances, 2,"bug_introducing");
			simpleClassify.Evaluation();
			for (double value: simpleClassify.getRes()) {
				System.out.print(value+"  ");
			}
			System.out.println();
			System.out.println();
		}

		
	}

	static void Auto() throws Exception {
		File file = new File("V2Arff");
		String[] arffFiles = file.list();
		File save = new File("Result");
		if (!save.exists()) {
			save.mkdir();
		}
		DecimalFormat df = new DecimalFormat("0.00");

		for (String string : arffFiles) {
			File arffFile = new File(file.getName() + "/" + string);
			ArffLoader arffLoader = new ArffLoader();
			arffLoader.setFile(arffFile);

			Instances instances = arffLoader.getDataSet();
			// PreProcess preProcess = new PreProcess();
			// instances = preProcess.NumLn(instances, "bug_introducing");
			ClassifyCalculate classifyCalculate = new ClassifyCalculate(
					instances, "bug_introducing");
			classifyCalculate.totalCal();
			Map<List<String>, List<Double>> resMap = classifyCalculate.getRes();
			File saverFile = new File(save.getName() + "/"
					+ string.replace(".arff", "result.csv"));
			if (saverFile.exists()) {
				saverFile.createNewFile();
			}
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(
					saverFile));
			for (List<String> key : resMap.keySet()) {
				for (String string2 : key) {
					bWriter.append(string2 + ",");
				}
				for (double dou : resMap.get(key)) {
					bWriter.append(df.format(dou) + ",");
				}
				bWriter.append("\n");
			}
			bWriter.flush();
			bWriter.close();
		}
	}
}
