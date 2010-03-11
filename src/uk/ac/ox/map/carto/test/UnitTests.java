package uk.ac.ox.map.carto.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.ox.map.carto.canvas.Rectangle;
import uk.ac.ox.map.carto.canvas.Rectangle.AnchorX;
import uk.ac.ox.map.carto.canvas.Rectangle.AnchorY;
import uk.ac.ox.map.carto.util.StringUtil;

public class UnitTests {
	@Test
	public void testStringUtil(){
		List<String> t = new ArrayList<String>();
		String s;
		t.add("a");
		s = StringUtil.getReadableList(t);
		System.out.println(s);
		assertTrue(s.compareTo("a") == 0);
		
		t.clear();
		t.add("a");
		t.add("b");
		s = StringUtil.getReadableList(t);
		System.out.println(s);
		assertTrue(s.compareTo("a and b") == 0);
		
		t.clear();
		t.add("a");
		t.add("b");
		t.add("c");
		s = StringUtil.getReadableList(t);
		System.out.println(s);
		assertTrue(s.compareTo("a, b and c") == 0);
		
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		s = StringUtil.getReadableList(l);
		System.out.println(s);
		assertTrue(s.compareTo("1, 2, 3 and 4") == 0);
	}
	@Test
	public void testLegend() {
		
	}
	

}
