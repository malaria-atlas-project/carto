package uk.ac.ox.map.carto.util;

import java.io.IOException;

public class SystemUtil {
	public static void addBranding(String fileName) throws IOException, InterruptedException {
		String command = "pdftk /home/will/map1_public/temp/branding.pdf background /tmp/tmp_mapsurface.pdf output /home/will/maps/pdf/%s.pdf";
		execCommand(String.format(command, fileName));
	}
	public static void convertToPng(String fileName) throws IOException, InterruptedException {
		String command = "pdftoppm -r 300 /home/will/maps/%s.pdf /tmp/map_pnm";
		execCommand(String.format(command, fileName));
		command =  "pnmtopng /tmp/map_pnm-1.ppm > /home/will/maps/png/%s.png";
		execCommand(String.format(command, fileName));
		
	}
	
	public static void execCommand(String command) throws IOException, InterruptedException {
		final Process process;
		process = Runtime.getRuntime().exec(command);
		int returnCode = process.waitFor();
		System.out.println(command);
		System.out.println("Return code = " + returnCode);
		
	}

}
