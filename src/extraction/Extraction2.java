package extraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
	List<String> attributes;
	TreeSet<List<Integer>> icf_id;
	Map<List<Integer>, List<Integer>> match;
	Map<List<Integer>, List<Integer>> cpMap;
	int startId;
	int endId;

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
		System.out.println("起始id号:" + startId + " 结束id号: " + endId);
		Get_icfId();
		attributes = new ArrayList<>(); // 属性判断的时候还可以从数据库读取数据.
	}

	/**
	 * 获取指定范围区间内文件所需要回溯的文件集合.
	 * 
	 * @throws SQLException
	 */
	private void Get_icfId() throws SQLException {
		if (startId > endId || startId < 0) {
			return;
		}

		icf_id = new TreeSet<>(new Comparator<List<Integer>>() {
			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		cpMap = new HashMap<List<Integer>, List<Integer>>();
		for (int i = startId; i <= endId; i++) {
			int curId = 0;
			int curCommitId = 0;
			int curFileId = 0;
			String file_name = null;

			int preId = 0;
			int preCommitId = 0;
			int preFileId = 0;

			sql = "select extraction1.id,extraction1.commit_id,extraction1.file_id,file_name from extraction1,files where extraction1.id="
					+ i + " and extraction1.file_id=files.id";
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				curId = resultSet.getInt(1);
				curCommitId = resultSet.getInt(2);
				curFileId = resultSet.getInt(3);
				file_name = resultSet.getString(4);
			}
			sql = "select id,commit_id,file_id from extraction1 where id=(select max(extraction1.id) from extraction1,files where extraction1.id<"
					+ curId
					+ " and extraction1.file_id=files.id and file_name='"
					+ file_name + "')";
			resultSet = stmt.executeQuery(sql);

			while (resultSet.next()) {
				preId = resultSet.getInt(1);
				preCommitId = resultSet.getInt(2);
				preFileId = resultSet.getInt(3);
			}

			List<Integer> cur = new ArrayList<>();
			cur.add(curId);
			cur.add(curCommitId);
			cur.add(curFileId);
			icf_id.add(cur);

			if (preId > 0) {
				List<Integer> pre = new ArrayList<>();
				pre.add(preId);
				pre.add(preCommitId);
				pre.add(preFileId);
				icf_id.add(pre);
				cpMap.put(cur, pre);
			}
		}
		System.out.println(icf_id.size());
		System.out.println(cpMap.size());
	}

	// startId和endId指的是要得到的数据的区间。如果两个参数为-1
	// 则表明对extraction1中的数据全部处理。
	/**
	 * 根据understand得到的复杂度文件filename提取选择出的各实例的复杂度信息。
	 * 
	 * @param filename
	 *            利用understand得到的各文件的复杂度文件，是一个单个文件。
	 * @param gap
	 *            利用字符串gap的值有效区分（commit_id,file_id）对。
	 * @throws FileNotFoundException
	 * @throws SQLException
	 */
	public void extraFromTxt(String filename, String gap)
			throws FileNotFoundException, SQLException {

		sql = "create table extraction2(id int(11) primary key not null auto_increment,commit_id int(11),file_id int(11))";
		stmt.executeUpdate(sql);

		Iterator<List<Integer>> iterator = icf_id.iterator();
		while (iterator.hasNext()) {
			List<Integer> list = iterator.next();
			sql = "insert extraction2 (commit_id,file_id) values("
					+ list.get(1) + "," + list.get(2) + ")";
			stmt.executeUpdate(sql);
		}

		Scanner in = new Scanner(new File(filename));
		String line = new String();
		while ((line = in.nextLine()) != null) {
			if (line.contains(gap)) {
				int commit_id = Integer.parseInt(line.substring(
						line.lastIndexOf("\\") + 1, line.lastIndexOf("."))
						.split("_")[0]);
				int file_id = Integer.parseInt(line.substring(
						line.lastIndexOf("\\") + 1, line.lastIndexOf("."))
						.split("_")[1]);
				System.out.println("commit_id " + commit_id + " file_id "
						+ file_id);
				String temp = in.nextLine();
				while (temp != null && (!temp.equals(""))) {
					String attr = temp.split(":")[0].replace(" ", ""); // 这里是不是可以用个正则表达式？
					double value = Double.parseDouble(temp.split(":")[1]
							.replace(" ", ""));
					if (!attributes.contains(attr)) {
						sql = "alter table extraction2 add column " + attr
								+ " float default 0";
						stmt.executeUpdate(sql);
						attributes.add(attr);
					}
					sql = "update extraction2 set " + attr + "=" + value
							+ " where commit_id=" + commit_id + " and file_id="
							+ file_id;
					stmt.executeUpdate(sql);
					if (in.hasNextLine()) {
						temp = in.nextLine();
					} else {
						temp = null;
					}
				}
			}
			if (!in.hasNextLine()) {
				break;
			}
		}
		in.close();
	}

	/**
	 * 根据understand得到的复杂度信息提取DeltMetrics。
	 * 
	 * @throws SQLException
	 */
	public void creatDeltMetrics() throws SQLException {
		System.out.println("get delta metrics");
		List<String> attribute2 = new ArrayList<>();
		if (attributes.size() > 0) { // 一口气的构造，直接从内存中读
			attribute2.addAll(attributes);
		} else { // 非要分看查看的话只能从数据库再重新读一次了。
			sql = "desc extraction2";
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				String attString = resultSet.getString(1);
				if (attString.equals("id") || attString.equals("commit_id") // 虽然每次取出来一个列标签都得判断，但是总共的列数不多，还不值得优化
						|| attString.equals("file_id")) {
					continue;
				}
				attribute2.add(resultSet.getString(1));
			}
		}

		for (String string : attribute2) {
			sql = "alter table extraction2 add column " + "Delta_" + string
					+ " float default 0";
			stmt.executeUpdate(sql);
			Iterator<List<Integer>> iterator = icf_id.iterator();
			while (iterator.hasNext()) {
				List<Integer> temp = iterator.next();
				float des = 0;

				if (cpMap.containsKey(temp)) {
					List<Integer> pre = cpMap.get(temp);
					sql = "select " + string
							+ " from extraction2 where commit_id=" + pre.get(1)
							+ " and file_id=" + pre.get(2);
					resultSet = stmt.executeQuery(sql); // 返回一个至用resultset太笨了
					while (resultSet.next()) {
						des = resultSet.getFloat(1);
					}
				}
				sql = "update extraction2 set Delta_" + string + "=" + string
						+ "-" + des + "where commit_id=" + temp.get(1)
						+ " and file_id=" + temp.get(2);
				stmt.executeUpdate(sql);
			}
		}
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

	/**
	 * 获取需要恢复的文件的rev.
	 * 
	 * @param file
	 * @throws SQLException
	 * @throws IOException
	 */
	public void GetCfTxt(String file) throws SQLException, IOException {
		Iterator<List<Integer>> iterator = icf_id.iterator();
		FileWriter fWriter = new FileWriter(new File(file), true);
		while (iterator.hasNext()) {
			sql = "select extraction1.commit_id,extraction1.file_id,rev,current_file_path from extraction1,scmlog,actions where extraction1.id="
					+ iterator.next().get(0)
					+ " and extraction1.commit_id=scmlog.id and  extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and type!='D'";
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				fWriter.write(resultSet.getInt(1) + "   ");
				fWriter.write(resultSet.getInt(2) + "   ");
				fWriter.write(resultSet.getString(3) + "   ");
				fWriter.write(resultSet.getString(4));
				fWriter.write("\n");
			}
		}
		fWriter.flush();
		fWriter.close();
	}

	public void deleteRedundance() throws SQLException {
		System.out.println("delete redundance");
		Set<Integer> remove = new HashSet<Integer>();
		sql = "select extraction2.id from extraction2,extraction1 where extraction2.commit_id=extraction1.commit_id and extraction2.file_id=extraction1.file_id and extraction1.id<"
				+ startId;
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			remove.add(resultSet.getInt(1));
		}
		sql = "select extraction2.id from extraction2,actions where extraction2.commit_id=actions.commit_id and actions.file_id=extraction2.file_id and type='D'";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			remove.add(resultSet.getInt(1));
		}
		for (Integer integer : remove) {
			sql = "delete from extraction2 where id=" + integer;
			stmt.execute(sql);
		}
	}
}
