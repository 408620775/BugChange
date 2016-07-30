package extraction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 从miningit生成的数据库中提取一些基本信息，例如作者姓名，提交时间，累计的bug计数等信息。 构造函数中提供需要连接的数据库。
 * 根据指定的范围获取commit_id列表（按照时间顺序）。通过对各表的操作获取一些基本数据。
 * 除了基本表，miningit还需执行extension=bugFixMessage，metrics
 * 
 * @param commitIdPart
 *            指定范围内的commit的id（按照时间排序）。由于数据库的存取会消耗大量时间,
 *            所以可以用该list来替代父类中的commit_ids
 *            这样使得提取某些属性值的时候可以只针对指定数据提取,而非所有的extraction1表中的数据
 *            ,以此节约时间.需要注意的是,累计的信息的提取,例如累计的bug计数,需要extraction1中所有的数据参与.
 * @author niu
 *
 */
public class Extraction1 extends Extraction {
	List<Integer> commitIdPart;

	/**
	 * 提取第一部分change info，s为指定开始的commit_id，e为结束的commit_id
	 * 
	 * @param database
	 *            指定的miningit生成数据的数据库。
	 * @param s
	 *            指定的commit的起始值
	 * @param e
	 *            指定的commit的结束值
	 * @throws Exception
	 */
	public Extraction1(String database, int s, int e) throws Exception {
		super(database, s, e);
		commitIdPart = new ArrayList<>();
		for (int j = start - 1; j < end; j++) {
			commitIdPart.add(commit_ids.get(j));
		}
	}

	/**
	 * 批量化执行若干函数。 此处四个获取信息的函数可以优化成一个，以减少时间开销，但是会增加代码长度。
	 * 
	 * @throws SQLException
	 */
	public void Carry1() throws SQLException {
		CreateTable();
		initial();
		author_name(false);
		commit_day(false);
		commit_hour(false);
		change_log_length(false);
	}

	/**
	 * 批量化执行若干函数。 防止Carry1责任过大，故将所有函数分为两部分执行。
	 * 
	 * @throws Exception
	 */
	public void Carry2() throws Exception {
		// sloc();
		cumulative_change_count();
		changed_LOC(false);
		bug_introducing();
		cumulative_bug_count();
	}

	/**
	 * 创建数据表extraction1。 若构造函数中所连接的数据库中已经存在extraction1表，则会产生冲突。
	 * 解决方案有2：（1）若之前的extraction1为本程序生成的表，则可将其卸载。
	 * （2）若之前的extraction1为用户自己的表，则可考虑备份原表的数据，并删除原表（建议），
	 * 或者重命名本程序中的extraction1的名称（不建议）。
	 * 
	 * @throws SQLException
	 */
	public void CreateTable() throws SQLException {
		sql = "create table extraction1(id int(11) primary key not null auto_increment,commit_id int(11),file_id int(11),author_name varchar(255),commit_day varchar(15),commit_hour int(2),"
				+ "cumulative_change_count int(15) default 0,cumulative_bug_count int(15) default 0,change_log_length int(10),changed_LOC int(13),"
				+ "sloc int(15),bug_introducing tinyint(1) default 0)";
		int result = stmt.executeUpdate(sql);
		if (result != -1) {
			System.out.println("创建表extraction1成功");
		}
	}

	/**
	 * 初始化表格。 根据指定范围内的按时间排序的commit列表（commit_ids）初始化extraction1。
	 * 初始化内容包括id，commit_id，file_id。需要注意的是，目前只考虑java文件，且不考虑java中的测试文件
	 * 所以在actions表中选择对应的项时需要进行过滤。参数表示想要提取file change信息的commit跨度
	 * 
	 * @throws SQLException
	 */
	public void initial() throws SQLException {
		System.out.println("initial the table");
		for (Integer integer : commit_ids) {
			sql = "select commit_id,file_id,file_name,current_file_path from actions,files where commit_id="
					+ integer + " and file_id=files.id"; // 只选取java文件,同时排除测试文件。
			resultSet = stmt.executeQuery(sql);
			List<List<Integer>> list = new ArrayList<>();
			while (resultSet.next()) {
				if (resultSet.getString(3).contains(".java")
						&& (!resultSet.getString(4).toLowerCase()
								.contains("test"))) { // 过滤不完全，如果是Test呢？
					List<Integer> temp = new ArrayList<>();
					temp.add(resultSet.getInt(1));
					temp.add(resultSet.getInt(2));
					list.add(temp);
				}
			}

			for (List<Integer> list2 : list) {
				sql = "insert extraction1 (commit_id,file_id) values("
						+ list2.get(0) + "," + list2.get(1) + ")";
				stmt.executeUpdate(sql);
			}
		}
	}

	/**
	 * 获取作者姓名。如果excuteAll为真,则获取extraction1中所有数据的作者.
	 * 否则只获取commit_id在commitIdPart中的数据的作者.
	 * 
	 * @throws SQLException
	 */
	public void author_name(boolean excuteAll) throws SQLException {
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		System.out.println("get author_name");
		for (Integer integer : excuteList) {
			sql = "update extraction1,scmlog,people set extraction1.author_name=people.name where extraction1.commit_id="
					+ integer
					+ " and extraction1.commit_id="
					+ "scmlog.id and scmlog.author_id=people.id";
			stmt.executeUpdate(sql);
		}

	}

	/**
	 * 获取提交的日期，以星期标示。如果excuteAll为真,则获取extraction1中所有数据的日期.
	 * 否则只获取commit_id在commitIdPart中的数据的日期.
	 * 
	 * @throws SQLException
	 */
	public void commit_day(boolean excuteAll) throws SQLException {
		System.out.println("get commit_day");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		Map<Integer, String> mapD = new HashMap<>(); // 加入修改日期
		for (Integer integer : excuteList) {
			sql = "select id,commit_date from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				mapD.put(resultSet.getInt(1),
						resultSet.getString(2).split(" ")[0]);
			}
		}

		// System.out.println(mapD.size()); //测试是否提取出时间，结果正确
		Calendar calendar = Calendar.getInstance();// 获得一个日历
		String[] str = { "Sunday", "Monday", "Tuesday", "Wednesday",
				"Thursday", "Friday", "Saturday", };
		for (Integer i : mapD.keySet()) {
			int year = Integer.parseInt(mapD.get(i).split("-")[0]);
			int month = Integer.parseInt(mapD.get(i).split("-")[1]);
			int day = Integer.parseInt(mapD.get(i).split("-")[2]);

			calendar.set(year, month - 1, day);// 设置当前时间,月份是从0月开始计算
			int number = calendar.get(Calendar.DAY_OF_WEEK);// 星期表示1-7，是从星期日开始，
			mapD.put(i, str[number - 1]);
			sql = "update extraction1 set commit_day=\" " + str[number - 1]
					+ "\" where commit_id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取提交的时间，以小时标示。如果excuteAll为真,则获取extraction1中所有数据的时间.
	 * 否则只获取commit_id在commitIdPart中的数据的时间.
	 * 
	 * @throws NumberFormatException
	 * @throws SQLException
	 */
	public void commit_hour(boolean excuteAll) throws NumberFormatException,
			SQLException {
		System.out.println("get commit_hour");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		Map<Integer, Integer> mapH = new HashMap<>(); // 加入修改时间
		for (Integer integer : excuteList) {
			sql = "select id,commit_date from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				mapH.put(resultSet.getInt(1), Integer.parseInt(resultSet
						.getString(2).split(" ")[1].split(":")[0]));
			}
		}

		Iterator<Entry<Integer, Integer>> iter = mapH.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> e = iter.next();
			int key = e.getKey();
			int value = e.getValue();
			sql = "update  extraction1 set commit_hour=" + value
					+ "  where commit_id=" + key;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取changlog的长度。如果excuteAll为真,则获取extraction1中所有数据的changlog长度.
	 * 否则只获取commit_id在commitIdPart中的数据的changelog长度.
	 * 
	 * @throws SQLException
	 */
	public void change_log_length(boolean excuteAll) throws SQLException {
		System.out.println("get change log length");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		for (Integer integer : excuteList) {
			sql = "select message from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			String message = null;
			while (resultSet.next()) {
				message = resultSet.getString(1);
			}
			sql = "update extraction1 set change_log_length ="
					+ message.length() + " where commit_id=" + integer;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取源码长度。 得到表metrics的复杂度开销很大，
	 * 而得到的信息在此后的extraction2中非常方便的提取，所以真心觉得此处提起这个度量没有什么意义。
	 * 
	 * @throws SQLException
	 */
	public void sloc() throws SQLException {
		System.out.println("get sloc");
		sql = "update extraction1,metrics set extraction1.sloc=metrics.loc where extraction1.commit_id=metrics.commit_id and "
				+ "extraction1.file_id=metrics.file_id";
		stmt.executeUpdate(sql);
	}

	/**
	 * 获取累计的bug计数。首先得判断出某个commit_id，file_id对应的那个文件是否是bug_introducing。
	 * 也就是本程序需要在bug_introducing之后执行.
	 * 
	 * @throws Exception
	 *             主要是为了想体验一下这个异常怎么用才加的，其实没啥用，因为bug_introducing非常不可能出现除0,1外的其他值。
	 */
	public void cumulative_bug_count() throws Exception {
		System.out.println("get cumulative bug count");
		sql = "select count(*) from extraction1";
		resultSet = stmt.executeQuery(sql);
		int totalNum = 0;
		while (resultSet.next()) {
			totalNum = resultSet.getInt(1);
		}
		Map<String, Integer> fileName_curBugCount = new HashMap<>();
		for (int i = 1; i <= totalNum; i++) {
			sql = "select file_name,bug_introducing from files,extraction1 where file_id=files.id and extraction1.id="
					+ i;
			resultSet = stmt.executeQuery(sql);
			String file_name = null;
			int bug_introducing = 0;
			while (resultSet.next()) {
				file_name = resultSet.getString(1);
				bug_introducing = resultSet.getInt(2);
			}
			if (bug_introducing == 1) {
				if (fileName_curBugCount.containsKey(file_name)) {
					fileName_curBugCount.put(file_name,
							fileName_curBugCount.get(file_name) + 1);
				} else {
					fileName_curBugCount.put(file_name, 1);
				}
			} else if (bug_introducing == 0) {
				if (!fileName_curBugCount.containsKey(file_name)) {
					fileName_curBugCount.put(file_name, 0);
				}
			} else {
				Exception e = new Exception(
						"class label is mistake! not 1 and not 0");
				e.printStackTrace();
				throw e;
			}
			sql = "update extraction1 set cumulative_bug_count="
					+ fileName_curBugCount.get(file_name) + " where id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取累计的change计数。
	 * 
	 * @throws SQLException
	 */
	public void cumulative_change_count() throws SQLException {
		System.out.println("get cumulative change count");
		sql = "select count(*) from extraction1";
		resultSet = stmt.executeQuery(sql);
		int totalNum = 0;
		while (resultSet.next()) {
			totalNum = resultSet.getInt(1);
		}
		Map<String, Integer> fileName_curChangeCount = new HashMap<>();

		for (int i = 1; i <= totalNum; i++) {
			sql = "select file_name from files,extraction1 where file_id=files.id and extraction1.id="
					+ i;
			resultSet = stmt.executeQuery(sql);
			String file_name = null;
			while (resultSet.next()) {
				file_name = resultSet.getString(1);
			}
			if (fileName_curChangeCount.containsKey(file_name)) {
				fileName_curChangeCount.put(file_name,
						fileName_curChangeCount.get(file_name) + 1);
			} else {
				fileName_curChangeCount.put(file_name, 1);
			}
			sql = "update extraction1 set cumulative_change_count="
					+ fileName_curChangeCount.get(file_name) + " where id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取改变的代码的长度。 主要从hunks中提取数据，如果在miningit中hunks运行两遍会导致hunks中数据有问题，出现重复项。
	 * 数据库中为null的项取出的数值是0,而不是空。
	 * 
	 * @throws SQLException
	 */
	public void changed_LOC(boolean excuteAll) throws SQLException {
		System.out.println("get changed loc");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		List<List<Integer>> re = new ArrayList<>();
		for (Integer integer : excuteList) {
			sql = "select id,file_id from extraction1 where commit_id="
					+ integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(integer);
				temp.add(resultSet.getInt(2));
				re.add(temp);
			}
		}
		for (List<Integer> list : re) {
			sql = "select old_start_line,old_end_line,new_start_line,new_end_line from hunks where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			resultSet = stmt.executeQuery(sql);
			int changeLoc = 0;
			while (resultSet.next()) {
				if (resultSet.getInt(1) != 0) {
					changeLoc = changeLoc + resultSet.getInt(2)
							- resultSet.getInt(1) + 1;
				}
				if (resultSet.getInt(3) != 0) {
					changeLoc = changeLoc + resultSet.getInt(4)
							- resultSet.getInt(3) + 1;
				}
			}
			sql = "update extraction1 set changed_LOC=" + changeLoc
					+ " where id=" + list.get(0);
			stmt.executeUpdate(sql); // 这个信息，似乎在extraction2中的detal计算时已经包含了啊。
		}
	}

	/**
	 * 相比于老的bug_introducing函数,此函数运行更快.
	 * 
	 * @throws SQLException
	 */
	public void bug_introducing() throws SQLException {
		System.out.println("get bug introducing");
		sql = "select hunks.id,file_name from hunks,files,"
				+ "(select commit_id as c,file_id as f from extraction1,scmlog where extraction1.commit_id=scmlog.id and is_bug_fix=1) as tb "
				+ "where hunks.commit_id=tb.c and hunks.file_id=tb.f and hunks.file_id=files.id;";
		resultSet = stmt.executeQuery(sql);
		Map<Integer, String> fId_name = new HashMap<>();
		while (resultSet.next()) {
			fId_name.put(resultSet.getInt(1), resultSet.getString(2));
		}
		for (Integer integer : fId_name.keySet()) {
			sql = "update extraction1,files set bug_introducing=1 where extraction1.file_id=files.id and file_name='"
					+ fId_name.get(integer)
					+ "' and commit_id IN (select bug_commit_id "
					+ "from hunk_blames where hunk_id=" + integer + ")";
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取类标号。 对于表extraction1中的每个实例（每一行内容）标识其是否为引入bug。bug_introducing为每个实例的类标签，用于
	 * 构建分类器。
	 * 
	 * @throws SQLException
	 */
	public void oldBug_introducing() throws SQLException {
		List<Integer> ids = new ArrayList<>();
		sql = "select id from scmlog where is_bug_fix=1";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			if (commit_ids.contains(resultSet.getInt(1))) {
				ids.add(resultSet.getInt(1));
			}
		}

		for (Integer integer : ids) {
			sql = "select  id,file_id from hunks where commit_id=" + integer;
			resultSet = stmt.executeQuery(sql);
			List<List<Integer>> hunkFileId = new ArrayList<>(); // 有些只是行错位了也会被标记为bug_introducing。但是作为hunks的一部分好像也成。
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(resultSet.getInt(2));
				hunkFileId.add(temp);
			}

			for (List<Integer> integer2 : hunkFileId) {
				sql = "update extraction1,files set  bug_introducing=1 where file_id=files.id and file_name= (select file_name from files where id="
						+ integer2.get(1)
						+ ")"
						+ " and commit_id IN (select bug_commit_id "
						+ "from hunk_blames where hunk_id="
						+ integer2.get(0)
						+ ")";
				stmt.executeUpdate(sql);
			}
		}
	}
}
