package uk.ac.ox.map.carto.style;

import com.vividsolutions.jts.geom.Point;


/**
 * 
 */
public class PointSymbolizer {
	private LineStyle lineStyle;
	private FillStyle fillStyle;
	private final Point point;
	
	public PointSymbolizer(Point p, FillStyle fs, LineStyle ls) {
		this.point = p;
		this.lineStyle = ls;
		this.fillStyle = fs;
    }
	
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

	public Point getPoint() {
		return point;
	}

}
