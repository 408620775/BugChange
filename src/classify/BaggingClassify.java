package classify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;

/**
 * bagging分类器. 该分类器由10个子分类器构成.在进行模型构建时有两种方式. 第一种方式是直接调用weka的bagging方法;
 * 第二种方式是手动完成10个子分类器训练集的构建,并整合10个子分类器的分类效果,其中训练集的构建分为过采样和欠采样两种方式.
 * 
 * @author niu
 *
 */
public class BaggingClassify extends Classify {
	MyBagging bagging;
	int choose; // 0标示普通的bagging方法,1表示undersample,2表示oversample

	/**
	 * 构造函数;指定用于构建bagging分类器的训练集以及子分类器类型.
	 * 
	 * @param classifier
	 *            子分类器.
	 * @param instances
	 *            bagging分类器的训练集.
	 * @param ou
	 * @throws Exception
	 */
	public BaggingClassify(Classifier classifier, Instances instances, String claName,int choose
			) throws Exception {
		super(classifier, instances, claName);
		 bagging = new MyBagging(classifier, instances, claName, choose);
		this.choose = choose;
		// SimpleClassify simpleClassify = new SimpleClassify(cla);

	}

	void Evaluation() throws Exception {
		res = new ArrayList<>();
		eval = new MyEvalution(ins,choose);
		eval.crossValidateModel(bagging, ins, 10, new Random(1));
		res.add(eval.recall(0));
		res.add(eval.recall(1));
		res.add(eval.precision(0));
		res.add(eval.precision(1));
		res.add(eval.fMeasure(0));
		res.add(eval.fMeasure(1));
		res.add(eval.areaUnderROC(1));
		res.add(Math.sqrt(res.get(0) * res.get(1)));
		return;
	}
}
