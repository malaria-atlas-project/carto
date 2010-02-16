package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;

/*
 * Yet another rectangle class.
 * All fields are double for flexibility.
 * 
 */
public class Rectangle {
	double x,y, width,height;
	public Rectangle(double x, double y, double width, double height) {
		this.x=x;
		this.y=y;
		this.width = width;
		this.height = height;
	}
	
	public Rectangle (double x, double y, double width, double height, AnchorX ax, AnchorY ay){
		
        switch(ax) {
            case L: break;
            case C: x-=(width/2); break;
            case R: x-= width; break;
        }
        switch(ay) {
            case T: break;
            case C: y-=(height/2); break;
            case B: y-=height; break;
		}
		this.x=x;
		this.y=y;
		this.width = width;
		this.height = height;
	}
	
	public enum AnchorX { L, C, R; }
	public enum AnchorY { T, C, B; }
	
	public Point2D.Double getUpperLeft() {
		return new Point2D.Double(x, y);
	}
}
