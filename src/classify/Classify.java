package classify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * 抽象分类器类.
 * 
 * @author niu
 *
 */
public class Classify {
	Classifier cla;
	Evaluation eval;
	Instances ins;
	List<Double> res;
	public String className = "bug_introducing";

	/**
	 * 返回分类器评估结果.
	 * 
	 * @return 模型评估结果
	 */
	public List<Double> getRes() {
		return res;
	}

	/**
	 * 返回分类器.
	 * 
	 * @return 使用的weka中的分类器.
	 */
	public Classifier getCla() {
		return cla;
	}

	/**
	 * 设置使用的分类器.
	 * 
	 * @param cla
	 *            要设置的分类器.
	 */
	public void setCla(Classifier cla) {
		this.cla = cla;
	}

	/**
	 * 返回构建分类器的训练集.
	 * 
	 * @return
	 */
	public Instances getIns() {
		return ins;
	}

	/**
	 * 设置构建分类器的训练集.
	 * 
	 * @param ins
	 */
	public void setIns(Instances ins) {
		this.ins = ins;
		ins.setClass(ins.attribute(className));
	}

	/**
	 * 分类器构造函数.
	 * 
	 * @param classifier
	 *            用于分类的分类器.
	 * @param instances
	 *            用于构建分类器的实例集.
	 * @param claName
	 *            用于分类的类标签,默认为bug_introducing.
	 */
	public Classify(Classifier classifier, Instances instances, String claName) {
		this.cla = classifier;
		this.ins = instances;
		this.className = claName;
		instances.setClass(instances.attribute(className));
	}

	/**
	 * 先通过分类器构造分类器类,稍后传入训练集.用于
	 * 
	 * @param classifier
	 */
	public Classify(Classifier classifier, String claName) {
		this.cla = classifier;
		this.className = claName;
	}

	public Classify(Instances instances, String claName) {
		this.ins = instances;
		this.className = claName;
	}

	void Evaluation(int choose) throws Exception {
		res = new ArrayList<>();
		List<List<Double>> TenRes = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			eval = new MyEvaluation(ins, choose);
			eval.crossValidateModel(cla, ins, 10, new Random(1));
			List<Double> tempResult = new ArrayList<>();
			tempResult.add(eval.recall(0));
			tempResult.add(eval.recall(1));
			tempResult.add(eval.precision(0));
			tempResult.add(eval.precision(1));
			tempResult.add(eval.fMeasure(0));
			tempResult.add(eval.fMeasure(1));
			tempResult.add(eval.areaUnderROC(1));
			tempResult.add(Math.sqrt(tempResult.get(0) * tempResult.get(1)));
			TenRes.add(tempResult);
		}

		for (int i = 0; i < 8; i++) {
			double temp = 0.0;
			for (int j = 0; j < 10; j++) {
				temp = temp + TenRes.get(j).get(i);
			}
			res.add(temp / 10);
		}

	}

}
