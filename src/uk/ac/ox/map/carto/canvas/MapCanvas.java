package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;

public class MapCanvas extends BaseCanvas {
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf);
	}
	
	public void drawDataFrame(DataFrame df, int x, int y){
		cr.setSource(df.getSurface(), x, y);
		cr.paint();
	}
	
}
