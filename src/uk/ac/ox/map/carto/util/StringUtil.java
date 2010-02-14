package uk.ac.ox.map.carto.util;

import java.util.Iterator;
import java.util.Set;

public class StringUtil {
	public static String getReadableList(Set<?> s){
		StringBuilder sb = new StringBuilder();
		String delim = "";
		
		for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
			Integer integer = (Integer) iterator.next();
			if (!iterator.hasNext())
				delim = " and ";
			sb.append(delim).append(integer);
			delim =", ";
		}
		
		return sb.toString();
	}

}
