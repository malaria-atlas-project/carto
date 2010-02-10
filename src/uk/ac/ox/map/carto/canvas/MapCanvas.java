package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;

public class MapCanvas {
	private final Context cr;
	
	public MapCanvas(PdfSurface pdf) {
		cr = new Context(pdf);
	}
}
