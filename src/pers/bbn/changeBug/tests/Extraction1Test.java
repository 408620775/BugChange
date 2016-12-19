package pers.bbn.changeBug.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import pers.bbn.changeBug.extraction.SQLConnection;

public class Extraction1Test {
	SQLConnection sqlL;
	String sql;
	@BeforeClass
	public final void setup() {
		sqlL=new SQLConnection("MyVoldemort");
		sqlL.connect();
	}
	@Test
	public final void testAuthor_name() {
		
	}

	@Test
	public final void testCommit_day() {
		
	}

	@Test
	public final void testCommit_hour() {
		
	}

	@Test
	public final void testChange_log_length() {
		
	}

	@Test
	public final void testSloc() {
		
	}

	@Test
	public final void testCumulative_bug_count() {
		
	}

	@Test
	public final void testCumulative_change_count() {
		
	}

	@Test
	public final void testChanged_LOC() {
		
	}

	@Test
	public final void testBug_introducing() {
		
	}

	@Test
	public final void testOldBug_introducing() {
		
	}

}
