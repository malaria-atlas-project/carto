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
	
	public static String formatPlaceName(String formatString, String areaTypeSing, String areaTypePlur, List<String> ls) {
		String areaTypeName;
		if (ls.size() == 1)
			areaTypeName = areaTypeSing;
		else if (ls.size() > 1) 
			areaTypeName = areaTypePlur;
		else
			throw new IllegalArgumentException("Empty list passed to format place name.");
		
		return String.format(formatString, areaTypeName, getReadableList(ls));
	
	}

}
