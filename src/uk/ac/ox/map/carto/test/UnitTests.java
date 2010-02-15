package uk.ac.ox.map.carto.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ox.map.carto.util.StringUtil;

public class UnitTests {
	@Test
	public void testStringUtil(){
		Set<String> t = new HashSet<String>();
		t.add("b");
		t.add("c");
		t.add("a");
		System.out.println(
	StringUtil.getReadableList(t)	
				);
		assertTrue(StringUtil.getReadableList(t) == "a, b and c");
	}

}
