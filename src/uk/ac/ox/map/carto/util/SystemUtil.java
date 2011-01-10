package uk.ac.ox.map.carto.util;

import java.io.IOException;

public class SystemUtil {
	public static void addVectorBranding(String anoName, String foreGround) throws IOException, InterruptedException {
		Process process;
		
		String command = "pdftk /home/will/map1_public/maps/" + foreGround + ".pdf background /tmp/tmp_mapsurface.pdf output /home/will/c/Temp/maps/vector/pdf/%s.pdf";
		command = String.format(command, anoName);
		System.out.println(command);
		process = Runtime.getRuntime().exec(command);
		int returnCode = process.waitFor();
		System.out.println("Return code = " + returnCode);
	}
	
	public static void addBranding(String mapType, String fileName, String parasite, String foreGround) throws IOException, InterruptedException {
		Process process;
		fileName = fileName.replace(" ", "_");
		mapType = mapType.replace(" ", "_");
		parasite = parasite.replace(" ", "_");
		
		String command = "pdftk /home/will/map1_public/maps/%s.pdf background /tmp/tmp_mapsurface.pdf output /home/will/c/Temp/maps/%s/pdf/%s_%s.pdf";
		command = String.format(command, foreGround, mapType, fileName, parasite);
		System.out.println(command);
		process = Runtime.getRuntime().exec(command);
		int returnCode = process.waitFor();
		System.out.println("Return code = " + returnCode);
		
	/*	
		command = "pdftoppm -r 300 /home/will/maps/pdf/%s_%s.pdf | pnmtopng  > /home/will/maps/png/%s.png";
		command = String.format(command, fileName, fileName);
		process = Runtime.getRuntime().exec(command);
		System.out.println(command);
		returnCode = process.waitFor();
		System.out.println("Return code = " + returnCode);
		*/
	}

}
