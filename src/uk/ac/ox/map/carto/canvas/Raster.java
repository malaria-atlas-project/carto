package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;

import org.gnome.gdk.Pixbuf;


public class Raster {
	private final Pixbuf pb;
	private final double cellSize;
	private final Point2D.Double ll;
	
	public Raster(Pixbuf pb, Point2D.Double ll, double cellSize) {
		this.pb = pb;
		this.cellSize = cellSize;
		this.ll = ll;
	}

	public Pixbuf getPixbuf() {
		return pb;
	}
	
	/**
	 * Returns the geographical coordinates of the image origin
	 * i.e. the top left.
	 * 
	 */
	public Point2D.Double getOrigin() {
		double x = ll.x; 
		double y = ll.y;
		y += pb.getHeight() * cellSize;
		return new Point2D.Double(x,y);
	}
	
	public double getCellSize() {
		return cellSize;
	}
}
