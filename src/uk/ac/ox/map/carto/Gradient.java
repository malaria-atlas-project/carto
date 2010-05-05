package uk.ac.ox.map.carto;

import java.io.IOException;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Format;
import org.freedesktop.cairo.ImageSurface;
import org.freedesktop.cairo.LinearPattern;

public class Gradient {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ImageSurface iS = new ImageSurface(Format.ARGB32, 20, 256);
		LinearPattern lp = new LinearPattern(10, 0, 10, 256); 
		lp.addColorStopRGB(0.0, 0.270588, 0.462745, 0.694117);
		lp.addColorStopRGB(0.5, 1, 0.996078431, 0.819607843);
		lp.addColorStopRGB(1, 0.870588235, 0.188235294, 0.152941176);
		Context cr = new Context(iS);
		cr.setSource(lp);
		cr.paint();
		iS.writeToPNG("/tmp/gradient.png");
		
		
	}

}
