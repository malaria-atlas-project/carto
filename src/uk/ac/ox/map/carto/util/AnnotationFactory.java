package uk.ac.ox.map.carto.util;

public class AnnotationFactory {
	public static String gridText(Integer ord, String axisText) {
		String anno =  "°";
		if (ord == 0) {
			anno = "0" + anno;
		} else if (ord > 0) {
				anno = ord + anno + axisText.charAt(1);
		} else if (ord < 0) {
				ord = 0 - ord;
				anno = ord + anno + axisText.charAt(0);
		}
		return anno;
	}

}
