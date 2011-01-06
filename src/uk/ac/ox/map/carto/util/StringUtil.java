package uk.ac.ox.map.carto.util;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
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
	public static String formatAdminString1(String formatString, List<Object[]> adminLevels, List<Integer> years) {
		
	    MessageFormat apiForm = new MessageFormat("{0}");
	    
		double[] yearsLimits = {0,1,2};
		String[] yearsPart = {"", "for the year {1}", "for the years {1}"};
		
		ChoiceFormat cfYear = new ChoiceFormat(yearsLimits, yearsPart);
		
		apiForm.setFormatByArgumentIndex(0, cfYear);
		Object[] testArgs = {years.size(), getReadableList(years)};
		String yearsText =  apiForm.format(testArgs);
		
//		return yearsText;
		return String.format(formatString, formatAdminString2(adminLevels), yearsText);
		
	}
	
	public static String formatAdminString2(List<Object[]> adminLevels) {
		
	    String formatString = "were available for {0} at {1} level";
		MessageFormat apiForm = new MessageFormat(formatString);
	    
		/*
		 * Complex api but works: provide limits and parts to ChoiceFormat which can be used to format a specified index
		 * 
		 */
		double[] adminLimits = {0,1,2};
		String[] nAdminPart = {"zero administrative units", "one administrative unit","{0,number} administrative units"};
		ChoiceFormat cfAdmin = new ChoiceFormat(adminLimits, nAdminPart);
		apiForm.setFormatByArgumentIndex(0, cfAdmin);
		
		Object[] adminLevel1 = adminLevels.get(0);
		Object[] testArgs = {adminLevel1[1],adminLevel1[0]};
		
		/*
		 * Start list with complicated text from formatter
		 */
		List<String> extraAdmin = new ArrayList<String>();
		extraAdmin.add(apiForm.format(testArgs));
		
		List<Object[]> sub = adminLevels.subList(1, adminLevels.size());
		
		for (Object[] adminLevel : sub) {
			String adLev = (String) adminLevel[0];
			Integer nAd = (Integer) adminLevel[1];
			if (adLev.equals("Admin0"))
				continue;
			extraAdmin.add(String.format("%s at %s", nAd, adLev));
		}
		
		return  StringUtil.getReadableList(extraAdmin);
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
	
	public static String formatExclusions(){
		return null;
	}

}
