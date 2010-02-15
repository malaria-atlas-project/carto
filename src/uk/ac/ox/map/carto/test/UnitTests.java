package uk.ac.ox.map.carto.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import uk.ac.ox.map.carto.util.StringUtil;

public class UnitTests {
	@Test
	public void testStringUtil(){
		List<String> t = new ArrayList<String>();
		t.add("a");
		assertTrue(StringUtil.getReadableList(t).compareTo("a") == 0);
		
		t.clear();
		t.add("a");
		t.add("b");
		assertTrue(StringUtil.getReadableList(t).compareTo("a and b") == 0);
		
		t.clear();
		t.add("a");
		t.add("b");
		t.add("c");
		assertTrue(StringUtil.getReadableList(t).compareTo("a, b and c") == 0);
		
	}

}
