package extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {
	String sql;
	SQLConnection sql1;
	Statement stmt;
	ResultSet resultSet;

	public Test(String database) {
		sql1 = new SQLConnection(database);
		this.stmt = sql1.getStmt();
	}

	public Test() {

	}

	public void testBugNum() throws SQLException {
		sql = "select commit_id,file_id,current_file_path from file_commit where is_bug_intro=1";
		resultSet = stmt.executeQuery(sql);
		int count = 0;
		while (resultSet.next()) {
			if (resultSet.getString(3).endsWith(".java")) {
				count++;
				System.out.println(resultSet.getInt(1) + "    "
						+ resultSet.getInt(2));
			}
		}
		System.out.println(count);
	}

	public void testBugNumMy() throws SQLException {
		sql = "select commit_id,file_id from extraction1 where bug_introducing=1";
		resultSet = stmt.executeQuery(sql);
		int count = 0;
		while (resultSet.next()) {
			count++;
			System.out.println(resultSet.getInt(1) + "    "
					+ resultSet.getInt(2));
		}
		System.out.println(count);
	}

	/**
	 * 测试对于给定的，bug_introducing=1的实例，使用反推的方法测试其是否真的是引入bug的实例。需要注意的是，由于分支的原因，
	 * 同一个文件的不同时期的file_id可能不同
	 * ,这虽然是少数情况，但确实是存在的，例如voldemort工程中，第commi_id=499，file_id
	 * =160的文件与commit_id=2927，file_id=3473的文件是对应的.
	 * 故，在实际获取bug_introducing时需要考虑的是file_name的对应关系（file_name出现重复的情况不多见）
	 * 
	 * @param commit_id
	 *            待测试实例的commit_id。
	 * @param file_id
	 *            待测试实例的file_id。
	 * @throws SQLException
	 */
	public void testBugIntro(int commit_id, int file_id) throws SQLException {
		String file_name = null;
		sql = "select file_name from files where id=" + file_id;
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			file_name = resultSet.getString(1);
		}
		sql = "select hunks.id,commit_id,file_id from hunks,files,scmlog where hunks.id in (select hunk_id from hunk_blames where bug_commit_id="
				+ commit_id
				+ ") and hunks.file_id=files.id and commit_id=scmlog.id and file_name='"
				+ file_name + "'" + " and is_bug_fix=1";
		resultSet = stmt.executeQuery(sql);
		List<List<Integer>> res = new ArrayList<>();
		while (resultSet.next()) {
			List<Integer> temp = new ArrayList<>();
			temp.add(resultSet.getInt(1));
			temp.add(resultSet.getInt(2));
			temp.add(resultSet.getInt(3));
			res.add(temp);
		}
		System.out.println("hunk_id,commit_id,file_id");
		for (List<Integer> list : res) {
			System.out.println(list);
		}
	}

	public void scmlogSort() throws SQLException {
		sql = "select * from scmlog order by commit_date";
		resultSet = stmt.executeQuery(sql);
		List<Integer> commit_id = new ArrayList<>();
		while (resultSet.next()) {
			commit_id.add(resultSet.getInt(1));
		}
		for (Integer integer : commit_id) {
			System.out.println(integer);
		}
	}

	/**
	 * 反推bow方法得到的字符串来自于哪些文件. 用于完善改正bow类.
	 * 
	 * @param special
	 *            给定的提取到的特殊字符串
	 * @param folder
	 *            提取词袋时指定的文件夹
	 * @throws IOException
	 */
	public static void specialWordFrom(String special, String folder)
			throws IOException {
		File file = new File(folder);
		File[] cFiles = file.listFiles();
		for (int i = 0; i < cFiles.length; i++) {
			BufferedReader br = new BufferedReader(new FileReader(cFiles[i]));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(special)) {
					System.out.println(cFiles[i].getName());
					break;
				}
			}
			br.close();
		}
	}

	/**
	 * 测试java文件名的独一性.
	 * 虽然存在一些重名的文件,但是经查(挑了几个查的,不完全保证),是由于不同分支导致的,而在actions表中他们的路径名是完全一样的
	 * ,也就是说根据file_name往回去找更合理.衍生的来说,实际上用于分类的file_id并没有file_name准确.
	 * 
	 * @throws SQLException
	 */
	public void testFileName() throws SQLException {
		sql = "select file_name from files";
		Set<String> names = new HashSet<>();
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			if (!names.add(resultSet.getString(1))
					&& resultSet.getString(1).contains(".java")) {
				System.out.println("the repeat file name "
						+ resultSet.getString(1));
			}
		}
	}
	/**
	 * 对于给定的commit_id,file_id,在extraction1中找到它上一次的change的信息,这些信息包括在extraction1中的id,commit_id,file_id.
	 * @param c_id
	 * @param f_id
	 * @throws SQLException
	 */
	public void GetLastChange(int c_id,int f_id) throws SQLException {
		sql="select extraction1.id,file_name from extraction1,files where extraction1.file_id=files.id and commit_id="+c_id+" and file_id="+f_id;
		resultSet=stmt.executeQuery(sql);
		int id=0;
		String file_name=null;
		while (resultSet.next()) {
			id=resultSet.getInt(1);
			file_name=resultSet.getString(2);
		}
		sql="select max(extraction1.id) from extraction1,files where extraction1.id<"+id+" and extraction1.file_id=files.id and file_name='"+file_name+"'";
		int lastId=0;
		resultSet=stmt.executeQuery(sql);
		while (resultSet.next()) {
			lastId=resultSet.getInt(1);
		}
		if (lastId>0) {
			sql="select id,commit_id,file_id from extraction1 where id="+lastId;
			resultSet=stmt.executeQuery(sql);
	        resultSet.next();
			System.out.println("last change of "+c_id+"_"+f_id+" is "+resultSet.getInt(2)+"_"+resultSet.getInt(3)+",and the id is "+resultSet.getInt(1));
		}else {
			System.out.println("this may be the first change!");
		}
	}
}
