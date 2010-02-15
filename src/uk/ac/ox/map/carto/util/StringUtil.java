package uk.ac.ox.map.carto.util;

import java.util.List;

public class StringUtil {
	public static <T> String getReadableList(List<T> years){
		StringBuilder sb = new StringBuilder();
		
		String delim = "";
		int i =0;
	    for (T t : years) {
	    	i++;
			if (i == years.size() && i > 1)
				delim =" and ";
			sb.append(delim).append(t);
			delim =", ";
		}
		
		return sb.toString();
	}

}
