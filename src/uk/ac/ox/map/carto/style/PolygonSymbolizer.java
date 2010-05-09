package uk.ac.ox.map.carto.style;


/**
 * 
 */
public class PolygonSymbolizer {
	private LineStyle lineStyle;
	private FillStyle fillStyle;
	
	public void setLineStyle(LineStyle lineStyle) {
	    this.lineStyle = lineStyle;
    }
	public LineStyle getLineStyle() {
	    return lineStyle;
    }
	public void setFillStyle(FillStyle fillStyle) {
	    this.fillStyle = fillStyle;
    }
	public FillStyle getFillStyle() {
	    return fillStyle;
    }
	

}
