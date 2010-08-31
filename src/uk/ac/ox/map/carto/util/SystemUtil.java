package uk.ac.ox.map.carto.util;

import java.io.IOException;

public class SystemUtil {
	public static void addVectorBranding(String anoName) throws IOException, InterruptedException {
		Process process;
		
		String command = "pdftk /home/will/map1_public/maps/branding_final.pdf background /tmp/tmp_mapsurface.pdf output /home/will/maps/vector/pdf/%s.pdf";
		command = String.format(command, anoName);
		System.out.println(command);
		process = Runtime.getRuntime().exec(command);
		int returnCode = process.waitFor();
		System.out.println("Return code = " + returnCode);
	}
	
	public static void addBranding(String fileName, String parasite, String orientation) throws IOException, InterruptedException {
		Process process;
		fileName = fileName.replace(" ", "_");
		
		String pdf;
		if (orientation.equals("portrait")) {
			pdf = "branding_final";
		} else {
			pdf = "branding_final_landscape";
		}
		
		String command = "pdftk /home/will/map1_public/maps/" + pdf + ".pdf background /tmp/tmp_mapsurface.pdf output /home/will/maps/pdf/%s_%s.pdf";
		command = String.format(command, fileName, parasite);
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
	
	public static void execCommand(String command) throws IOException, InterruptedException {
		final Process process;
		process = Runtime.getRuntime().exec(command);
		int returnCode = process.waitFor();
		System.out.println(command);
		System.out.println("Return code = " + returnCode);
	}

}
