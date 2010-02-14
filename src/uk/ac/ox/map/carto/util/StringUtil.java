package uk.ac.ox.map.carto.util;

import java.util.Iterator;
import java.util.Set;

public class StringUtil {
	public static <T> String getReadableList(Set<T> s){
		StringBuilder sb = new StringBuilder();
		String delim = "";
		
		for (Iterator<T> iterator = s.iterator(); iterator.hasNext();) {
			T obj = iterator.next();
			if (!iterator.hasNext())
				delim = " and ";
			sb.append(delim).append(obj);
			delim =", ";
		}
		
		return sb.toString();
	}

}
