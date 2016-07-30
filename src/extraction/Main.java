package extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class Main {
	static FileOperation fileOperation;

	public static void main(String[] args) throws Exception {
//		Test test = new Test("voldemort");
//		test.getCPC("voldeFault.csv");
//Extraction1 extraction1=new Extraction1("MyLucene", 1001, 1500);
//extraction1.Carry1(); // //所有的commit都要处理。
//extraction1.Carry2();

		//Automatic1("synapse",1001, 1500);
	//	Extraction2 extraction2 = new Extraction2("MySynapse", 1001,
	//	1500);
		// extraction2.Get_icfId();
	//	extraction2.recoverPreFile("synapseFiles");
		Automatic2("synapse", 1001, 1500);
		

//		BufferedReader bReader=new BufferedReader(new FileReader(new File("synapseFiles/328_1167.java")));
//		StringBuffer linebuffer=new StringBuffer();
//		String line=null;
//		while ((line=bReader.readLine())!=null) {
//			linebuffer.append(line+"\n");
//		}
//		bReader.close();
//		Bow bow=new Bow();
//		Map<String, Integer> contentMap=bow.bowP(linebuffer);
//		System.out.println(contentMap);
	}

	static public void Automatic1(String project, int start_commit_id,
			int end_commit_id) throws Exception {
		String database = "My"
				+ project.toLowerCase().substring(0, 1).toUpperCase()
				+ project.toLowerCase().substring(1);
		Extraction1 extraction1 = new Extraction1(database, start_commit_id, end_commit_id);
		extraction1.Carry1();
		extraction1.Carry2();

		Extraction2 extraction2 = new Extraction2(database, start_commit_id,
				end_commit_id);
		extraction2.Get_icfId();
		// Process process = Runtime.getRuntime().exec(
		// "/bin/sh /home/niu/workspace/changeClassify/src/extraction/GetFile.sh "
		// + project);
		// System.out.println("the exit value of process is "
		// + process.exitValue());
	}

	static public void Automatic2(String project, int start_commit_id,
			int end_commit_id) throws SQLException, IOException {
		String database = "My"
				+ project.toLowerCase().substring(0, 1).toUpperCase()
				+ project.toLowerCase().substring(1);
		Extraction2 extraction2 = new Extraction2(database, start_commit_id,
				end_commit_id);
		String metric = database + "Metrics.txt";
		extraction2.extraFromTxt(metric);

		Extraction3 extraction3 = new Extraction3(database,
				extraction2.getId_commitId_fileIds(), project + "Files");
		fileOperation = new FileOperation();
		Merge merge = new Merge(extraction3.getContent(),
				extraction2.getContentMap(),
				extraction2.getId_commitId_fileIds(), database);
		fileOperation.writeContent(merge.merge123(), database + ".csv");
		fileOperation.writeDict(database + "Dict.txt",
				extraction3.getDictionary());
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

}
