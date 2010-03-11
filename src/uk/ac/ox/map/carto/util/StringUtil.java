package uk.ac.ox.map.carto.util;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
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
	public static String formatAdminString(String formatString, List<String> adminLevels, List<Integer> years, Integer nAdminUnits) {
		
		/*
		 * 
	    	 {"apiText","The health management information system data used to inform the stable, unstable and " +
	    	 		"malaria free categories were available for %s administrative units at %s level for the following %s: %s."},
		 */
		
	    MessageFormat apiForm = new MessageFormat(formatString);
	    
		double[] adminLimits = {0,1,2};
		String[] nAdminPart = {"zero administrative units", "one administrative unit","{0,number} administrative units"};
		ChoiceFormat cfAdmin = new ChoiceFormat(adminLimits, nAdminPart);
		apiForm.setFormatByArgumentIndex(0, cfAdmin);
		
		double[] yearsLimits = {0,1,2};
		String[] yearsPart = {"", " for the year {3}", " for the years {3}"};
		ChoiceFormat cfYear = new ChoiceFormat(yearsLimits, yearsPart);
		apiForm.setFormatByArgumentIndex(2, cfYear);
		
		String adminLevelStr = getReadableList(adminLevels);
		Object[] testArgs = {nAdminUnits, adminLevelStr, years.size(), getReadableList(years)};
	 
		return apiForm.format(testArgs);
		
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
