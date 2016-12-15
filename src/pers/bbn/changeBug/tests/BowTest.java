package pers.bbn.changeBug.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import pers.bbn.changeBug.extraction.Bow;

public class BowTest {

	@Test
	public void testBow() throws IOException {
		File javaFile=new File("src/pers/bbn/changeBug/resources/ApplicationListener.txt");
		StringBuffer sBuffer=new StringBuffer();
		BufferedReader bReader=new BufferedReader(new FileReader(javaFile));
		String line=null;
		while ((line=bReader.readLine())!=null) {
			sBuffer.append(line);
		}
		bReader.close();
		Map<String, Integer> resMap=Bow.bow(sBuffer.toString());
		for (Map.Entry<String, Integer> element: resMap.entrySet()) {
			System.out.println(element.getKey()+"   "+element.getValue());
		}
		//fail("Not yet implemented");
	}

}
