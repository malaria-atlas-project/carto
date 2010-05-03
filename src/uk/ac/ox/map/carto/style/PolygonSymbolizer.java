package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.carto.canvas.style.FillStyle;
import uk.ac.ox.map.carto.canvas.style.LineStyle;

/*
 * TODO: actually use somewhere! use to fix drawing polygon duplication
 * due to extra styles
 */
public class PolygonSymbolizer {
	private LineStyle lineStyle;
	private FillStyle fillStyle;
	public void setFillStyle(FillStyle fillStyle) {
		this.fillStyle = fillStyle;
	}
	public FillStyle getFillStyle() {
		return fillStyle;
	}
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}
	public LineStyle getLineStyle() {
		return lineStyle;
	}

}
