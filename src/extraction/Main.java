package extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class Main {
	static FileOperation fileOperation;

	public static void main(String[] args) throws Exception {
		/*Test test=new Test();
		test.compareData("tf.csv", "myVoldemort.csv", 723, 1165);*/
		Extraction1 extraction1 = new Extraction1("eclipse", -1, -1); // 虽然只选取了300次commit，为了辨识这300次commit，
		extraction1.Carry1(); // //所有的commit都要处理。
		extraction1.Carry2();
		
//		Extraction2 extraction2=new Extraction2("voldemort",501, 800);
//		extraction2.extraFromTxt("voldeMetrics.txt");
//		Extraction3 extraction3 = new Extraction3("voldemort",
//				"voldeFiles", -1, -1);
//		fileOperation = new FileOperation();
//		Merge merge = new Merge(extraction3.getContent(), "voldemort");
//		fileOperation.writeContent(merge.merge123(), "myVoldemort.csv");
//		fileOperation.writeDict("dict.txt", extraction3.getDictionary());
	}

	static public void Automatic(String database) throws Exception {
		
		Extraction1 extraction1 = new Extraction1(database, -1, -1); // 虽然只选取了300次commit，为了辨识这300次commit，
		extraction1.Carry1(); // //所有的commit都要处理。
		extraction1.Carry2();

		Extraction2 extraction2 = new Extraction2("eclipse", 10001, 10500);
		extraction2.extraFromTxt("metrics.txt");
		extraction2.creatDeltMetrics();

		Extraction3 extraction3 = new Extraction3("eclipse",
				"/home/niu/test/eclipseProject", -1, -1);
		fileOperation = new FileOperation();
		Merge merge = new Merge(extraction3.getContent(), "eclipse");
		fileOperation.writeContent(merge.merge123(), "eclipse.csv");
		fileOperation.writeDict("dict.txt", extraction3.getDictionary());
	}

	static public void testMatch() {
		String test = new String("http://hello.com");
		String[] arrayStrings = test.split("//");
		for (String string : arrayStrings) {
			System.out.println(string);
		}
	}

	static public void testBow(String fileName) throws IOException {
		BufferedReader brReader = new BufferedReader(new FileReader(new File(
				fileName)));
		String line;
		StringBuffer text = new StringBuffer();
		while ((line = brReader.readLine()) != null) {
			text.append(line + "\n");
		}
		brReader.close();
		Bow bow = new Bow();
		Map<String, Integer> bag = bow.bowP(text);
		for (String s : bag.keySet()) {
			System.out.println(s + "    " + bag.get(s));
		}
	}

	

	public static void exect2() throws SQLException, IOException {
		Extraction2 extraction2 = new Extraction2("try", 1000, 1499);
		extraction2.extraFromTxt("/home/niu/test/metrics.txt");
		extraction2.creatDeltMetrics();
	}
}
