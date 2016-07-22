package extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 针对某分为内的commit_id的file_id，提取understand得到的复杂度信息。
 * 为了防止程序规模过大，一般在进行bug分类的时候，数据选取extraction1中一部分实例。而extraction2则是针对extraction1
 * 中选择出的实例利用understand得到这些实例对应的文件的复杂度量。
 * 
 * @param attribute
 *            存储所有属性的列表
 * @param startId
 *            extraction1中选取实例范围的起始id，注意，不是起始的commit_id。
 * @param endId
 *            extraction1中选取实例范围的终止id。
 * @author niu
 *
 */
public class Extraction2 extends Extraction {

	TreeSet<List<Integer>> icf_id;
	Map<List<Integer>, List<Integer>> match;
	Map<List<Integer>, List<Integer>> cpMap;
	int startId;
	int endId;
	Set<String> curFiles;
	Set<String> preFiles;
	Set<String> attributes;
	Map<String, Map<String, Double>> grid;

	// 不包括id,commit_id,file_id
	/**
	 * 构造函数,通过sCommitId和eCommitId确定要提取的数据的区间.
	 * 
	 * @param database
	 * @param sCommitId
	 * @param eCommitId
	 * @throws SQLException
	 */
	public Extraction2(String database, int sCommitId, int eCommitId)
			throws SQLException {
		super(database);
		int i = 1;
		while (sCommitId - i > 0) {
			sql = "select min(id) from extraction1 where commit_id="
					+ commit_ids.get(sCommitId - i);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				startId = resultSet.getInt(1);
			}
			if (startId != 0) {
				break;
			}
			i--;
		}
		System.out.println("the start commit_id is "
				+ commit_ids.get(sCommitId - i));
		i = 1;
		while (eCommitId - i > 0) {
			sql = "select max(id) from extraction1 where commit_id="
					+ commit_ids.get(eCommitId - i);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				endId = resultSet.getInt(1);
			}
			if (endId != 0) {
				break;
			}
			i++;
		}
		System.out.println("the end commit_id is "
				+ commit_ids.get(eCommitId - i));
		System.out.println("起始id号:" + startId + " 结束id号: " + endId);

	}

	/**
	 * 获取指定范围区间内文件集合. 该集合只包含了当前文件集合,并不包含每个文件的上一次提交.每个文件的上一次提交需要根据patch恢复.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void Get_icfId() throws SQLException, IOException {
		File file = new File("cfrc.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
		if (startId > endId || startId < 0) {
			bWriter.close();
			return;
		}
		sql = "select extraction1.commit_id,extraction1.file_id,rev,current_file_path from extraction1,scmlog,actions where extraction1.id>="
				+ startId
				+ " and extraction1.id<="
				+ endId
				+ " and extraction1.commit_id=scmlog.id and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and type!='D'";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			bWriter.append(resultSet.getInt(1) + "   " + resultSet.getInt(2)
					+ "   " + resultSet.getString(3) + "   "
					+ resultSet.getString(4));
			bWriter.append("\n");
		}
		bWriter.flush();
		bWriter.close();
	}

	public void recoverPreFile(String dictory) throws SQLException, IOException {
		File fFlie = new File(dictory);
		if (!fFlie.isDirectory()) {
			System.out.println("当前目录不是文件夹!");
			return;
		}
		String[] cFiles = fFlie.list();
		for (String string : cFiles) {
			getPreFile(dictory, string);
		}
	}

	/**
	 * 根据curFile和数据库中的patch信息,恢复得到preFile.
	 * 
	 * @param dictory
	 * @param string
	 * @throws SQLException
	 * @throws IOException
	 */
	public void getPreFile(String dictory, String string) throws SQLException,
			IOException {
		File curFile = new File(dictory + "/" + string);
		BufferedReader bReader = new BufferedReader(new FileReader(curFile));
		int commit_id = Integer.parseInt(string.split("_")[0]);
		int file_id = Integer.parseInt(string.split("\\.")[0].split("_")[1]);
		File preFile = new File(dictory + "/" + commit_id + "_" + file_id
				+ "_pre.java");
		if (!preFile.exists()) {
			preFile.createNewFile();
		}
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(preFile));
		int readIndex = 0;

		sql = "select patch from patches where commit_id=" + commit_id
				+ " and file_id=" + file_id;
		resultSet = stmt.executeQuery(sql);
		String patch = null;
		while (resultSet.next()) {
			patch = resultSet.getString(1);
		}
		if (patch == null) {
			System.out.println("the patch of " + curFile + " is null");
			String line=null;
			while ((line=bReader.readLine())!=null) {
				bWriter.append(line+"\n");
			}
			bReader.close();
			bWriter.flush();
			bWriter.close();
			return;
		}
		System.out.println(curFile);
		String[] lines = patch.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("---") || lines[i].startsWith("+++")) {
				continue;
			}
			if (lines[i].startsWith("@@")) {
				String lineIndex = (String) lines[i].subSequence(
						lines[i].indexOf("+") + 1, lines[i].lastIndexOf("@@"));

				int index = Integer.parseInt(lineIndex.split(",")[0].trim());
				int shiftP = Integer.parseInt(lineIndex.split(",")[1].trim());
				int shiftF = shiftP;

				while (readIndex < index - 1) {
					String line = bReader.readLine();
					bWriter.append(line + "\n");
					readIndex++;
				}
				i++;
				while (i < lines.length && (!lines[i].startsWith("@@"))) {
					if (lines[i].startsWith("-")) {
						bWriter.append(lines[i].substring(1, lines[i].length())
								+ "\n");
					} else if (lines[i].startsWith("+")) {

					} else {
						bWriter.append(lines[i] + "\n");
					}
					i++;
				}
				readIndex = readIndex + shiftF;
				for (int j = 0; j < shiftF; j++) {
					bReader.readLine();
				}
				i = i -1;
			}
		}

		String nextLineString = null;
		while ((nextLineString = bReader.readLine()) != null) {
			bWriter.append(nextLineString + "\n");
		}
		bReader.close();
		bWriter.flush();
		bWriter.close();
	}

	// startId和endId指的是要得到的数据的区间。如果两个参数为-1
	// 则表明对extraction1中的数据全部处理。
	/**
	 * 根据understand得到的复杂度文件filename提取选择出的各实例的复杂度信息。
	 * 
	 * @param MetricFile
	 *            利用understand得到的各文件的复杂度文件，是一个单个文件。
	 * @param gap
	 *            利用字符串gap的值有效区分（commit_id,file_id）对。
	 * @throws SQLException
	 * @throws IOException
	 */
	public void extraFromTxt(String MetricFile) throws SQLException,
			IOException {
		System.out.println("构建初始的复杂度标示");
		curFiles = new LinkedHashSet<>();
		preFiles = new HashSet<>();
		attributes = new LinkedHashSet<>();
		grid = new HashMap<>();
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				MetricFile)));
		String line = null;
		while ((line = bReader.readLine()) != null) {
			if (line.contains("File:")) {
				String fileName = (String) line.subSequence(
						line.lastIndexOf('\\') + 1, line.lastIndexOf(' '));
				if (!fileName.contains("pre")) {
					curFiles.add(fileName);
				} else {
					preFiles.add(fileName);
				}

				while ((line = bReader.readLine()) != null
						&& (!line.contains("File:")) && (!line.equals(""))) {
					line = line.trim();
					String attribute = line.split(":")[0];
					double value = Double
							.parseDouble(line.split(":")[1].trim());
					if (attributes.contains(attribute)) {
						grid.get(attribute).put(fileName, value);
					} else {
						attributes.add(attribute);
						Map<String, Double> temp = new HashMap<>();
						temp.put(fileName, value);
						grid.put(attribute, temp);

					}
				}
			}
		}
		bReader.close();
		creatDeltMetrics();
		createDatabase();
	}

	private void createDatabase() throws SQLException {
		System.out.println("将复杂度数据写如数据库");
		sql = "create table extraction2(id int(11) primary key not null auto_increment,commit_id int(11),file_id int(11))";
		stmt.executeUpdate(sql);
		for (String files : curFiles) {
			int commit_id = Integer.parseInt(files.split("_")[0]);
			int file_id = Integer.parseInt(files.substring(0,
					files.indexOf('.')).split("_")[1]);
			sql = "insert extraction2 (commit_id,file_id) values(" + commit_id
					+ "," + file_id + ")";
			stmt.executeUpdate(sql);
		}
		for (String attr : attributes) {
			sql = "alter table extraction2 add column " + attr
					+ " float default 0";
			stmt.executeUpdate(sql);
			for (String file : curFiles) {
				int commit_id = Integer.parseInt(file.split("_")[0]);
				int file_id = Integer.parseInt(file.substring(0,
						file.indexOf('.')).split("_")[1]);
				Double value = grid.get(attr).get(file);
				if (value == null) {
					value = 0.0;
				}
				sql = "update extraction2 set " + attr + "=" + value
						+ " where commit_id=" + commit_id + " and file_id="
						+ file_id;
				stmt.executeUpdate(sql);
			}
		}

	}

	/**
	 * 根据understand得到的复杂度信息提取DeltMetrics。
	 * 
	 * @throws SQLException
	 */
	public void creatDeltMetrics() throws SQLException {
		System.out.println("构造delta复杂度");
		Set<String> deltaArrSet = new HashSet<>();
		for (String attribute : attributes) {
			String deltaAttri = attribute + "_delta";
			deltaArrSet.add(deltaAttri);

			Map<String, Double> deltaMap = new HashMap<>();
			for (String cur : curFiles) {
				String preName = cur.substring(0, cur.indexOf('.'))
						+ "_pre.java";
				double value1 = 0;
				if (grid.get(attribute).containsKey(cur)) {
					value1 = grid.get(attribute).get(cur);
				}
				double value2 = 0;
				if (grid.get(attribute).containsKey(preName)) {
					value2 = grid.get(attribute).get(preName);
				}
				double delta = value1 - value2;
				deltaMap.put(cur, delta);
			}
			grid.put(deltaAttri, deltaMap);
		}
		attributes.addAll(deltaArrSet);
	}

	/**
	 * 显示当前数据库中的表有哪些
	 * 
	 * @throws SQLException
	 */
	public void Show() throws SQLException {
		sql = "show tables";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			System.out.println(resultSet.getString(1));
		}
	}

	/**
	 * 提取extraction2中的id，commit_id，file_id，用于Merge中的merge12()场景。
	 * 
	 * @return extraction2中的id，commit_id，file_id的列表。
	 * @throws SQLException
	 */
	public List<List<Integer>> GetId_commit_file() throws SQLException {
		List<List<Integer>> res = new ArrayList<>();
		sql = "select id,commit_id,file_id from extraction2";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			List<Integer> temp = new ArrayList<>();
			temp.add(resultSet.getInt(1));
			temp.add(resultSet.getInt(2));
			temp.add(resultSet.getInt(3));
			res.add(temp);
		}
		return res;
	}

}
