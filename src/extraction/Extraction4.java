package extraction;

import java.io.IOException;
import java.sql.SQLException;

public class Extraction4 extends Extraction {

	public Extraction4(String database, int s, int e) throws Exception {
		super(database, s, e);
	}

	public void printRevInfo(String outFile) throws SQLException, IOException {
		int idsInEx1 = 0;
		int finds = start-1;
		while (idsInEx1 == 0) {
			sql = "select min(id) from extraction1 where commit_id="
					+ commit_ids.get(finds);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				idsInEx1 = resultSet.getInt(1);
			}
			if (idsInEx1 == 0) {
				finds++; // warning: change the value of start
			}
		}
		int ideInEx1 = 0;
		int finde = end - 1;
		while (ideInEx1 == 0) {
			sql = "select max(id) from extraction1 where commit_id="
					+ commit_ids.get(finde);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				ideInEx1 = resultSet.getInt(1);
			}
			if (ideInEx1 == 0) {
				finde--; // warning: change the value of start
			}
		}

		sql = "select extraction1.commit_id,extraction1.file_id,current_file_path,rev from extraction1,actions,scmlog "
				+ "where extraction1.id>="
				+ idsInEx1
				+ " and extraction1.id<="
				+ ideInEx1
				+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and type!='D' and extraction1.commit_id=scmlog.id";
		resultSet = stmt.executeQuery(sql);
		StringBuffer sBuffer = new StringBuffer();
		String line = null;
		while (resultSet.next()) {
			line = resultSet.getInt(1) + "   " + resultSet.getInt(2) + "   "
					+ resultSet.getString(3) + "   " + resultSet.getString(4)
					+ "\n";
			sBuffer.append(line);
		}

		FileOperation.writeStringBuffer(sBuffer, outFile);
	}

	public static void main(String[] args) throws Exception {
		Extraction4 extraction4 = new Extraction4("MyCamel", 2501, 2800);
		extraction4.printRevInfo("CamelInfo");

	}

}
