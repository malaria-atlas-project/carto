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
	@Test
	public void testAnchorEnum(){
		/*
		 * 
		 */
		Rectangle rect = new Rectangle(10,10,50,30, AnchorX.C, AnchorY.B);
		
	}
	

}
