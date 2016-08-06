package classify;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

/**
 * 分类计算类。用于计算各种分类模型下的结果。
 * 
 * @param classifys
 *            使用的分类器模型。
 * @param methods
 *            使用的采样模型。
 * @author niu
 *
 */
public class ClassifyCalculate {
	String[] classifys = { "weka.classifiers.trees.J48",
			"weka.classifiers.bayes.NaiveBayes",
			"weka.classifiers.functions.SMO"};
			//"weka.classifiers.meta.AdaBoostM1" };
	String[] methods = { "standard", "undersample", "oversample", 
			"bagging", "underBagging", "overBagging"};
	Instances ins;
	Map<List<String>, List<Double>> res;

	public Map<List<String>, List<Double>> getRes() {
		return res;
	}

	String className = "bug_introducing";

	/**
	 * 构造函数，初始化要使用的用例集。
	 * 
	 * @param instances
	 *            将要用于分类的用力集，可能是不均衡的数据。
	 */
	public ClassifyCalculate(Instances instances, String claName) {
		this.ins = instances;
		this.className = claName;
		res = new LinkedHashMap<>();
	}

	/**
	 * 针对不同的分类器不同的采样方法,获取不同情况下的分类评估结果.
	 * 
	 * @throws Exception
	 */
	public void totalCal() throws Exception {
		//subInstances.add(sample.smote(ins));
		Classify classify = null;
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				List<String> keyList = new ArrayList<>();
				keyList.add(classifys[i]);
				keyList.add(methods[j]);
				classify = new SimpleClassify((Classifier) Class.forName(
						classifys[i]).newInstance(), ins,
						className,j);
				classify.Evaluation(j-3);
				res.put(keyList, classify.getRes());
			}

			for (int j = 3; j < 6; j++) {
				List<String> keyList = new ArrayList<>();
				keyList.add(classifys[i]);
				keyList.add(methods[j]);
				MyBagging bagging=new MyBagging((Classifier) Class.forName(
						classifys[i]).newInstance(), ins, className, j-3);
//				bagging.setClassifier((Classifier) Class.forName(
//						classifys[i]).newInstance());
				classify = new BaggingClassify(bagging, ins, className, j - 3);
//				classify=new BaggingClassify2((Classifier) Class.forName(
//					classifys[i]).newInstance(), subInstances.get(j-3), className);
				classify.Evaluation();
				res.put(keyList, classify.getRes());
			}
		}

/*		for (int i = 0; i < 4; i++) {
			List<String> keyList = new ArrayList<>();
			keyList.add(classifys[3]);
			keyList.add(methods[i]);
			classify=new BoostingClassify(subInstances.get(i), className);
			classify.Evaluation();
			res.put(keyList, classify.getRes());
		}*/
		
		DecimalFormat df = new DecimalFormat("0.00");
		for (List<String> m : res.keySet()) {
			for (String string : m) {
				System.out.print(string + "  ");
			}
			for (Double value : res.get(m)) {
				System.out.print(df.format(value) + "  ");
			}
			System.out.println();
		}
	}
}
