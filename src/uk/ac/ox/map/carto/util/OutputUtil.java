package uk.ac.ox.map.carto.util;

public class OutputUtil {
	static final String outputLoc = "/home/will/c/Temp/maps/%s/pdf/%s.pdf";
  
  public static String getOutputLocation(String project, String fileName) {
    return String.format(outputLoc, project, fileName);
  }

}
