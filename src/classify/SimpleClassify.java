package classify;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * weka分类器，采用十折交叉验证评估分类结果。
 * 
 * @param instances
 *            用于分类的实例。
 * @param classifier
 *            用于分类的分类器。
 * @param evaluation
 *            用于评估分类模型。
 * @param res
 *            存储分类后得到的若干指标。该数组中的值按索引从小到大依次对应于
 *            C-Recall，B-Recall，C-Precision，B-Precision
 *            ，C-Fmesure，B-Fmeasure，AUC，Gmean
 * @author niu
 *
 */
public class SimpleClassify extends Classify{
int choose;
	/**
	 * 构造函数。
	 * 
	 * @param ins
	 *            用于分类的实例。
	 * @param classAttri
	 *            类属性。
	 * @param cla
	 *            使用的分类器。
	 */
	public SimpleClassify( Classifier cla,Instances ins,String claName,int choose) {
		super(cla, ins,claName);
		this.choose=choose;
		ins.setClass(ins.attribute(className));
	}

	public SimpleClassify(Classifier cla,String claName) {
		super(cla,claName);
	}
}
