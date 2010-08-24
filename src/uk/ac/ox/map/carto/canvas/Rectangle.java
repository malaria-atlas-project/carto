package uk.ac.ox.map.carto.canvas;

import java.awt.geom.Point2D;

/**
 * Yet another rectangle class. All fields are double for flexibility.
 * 
 * @param x
 * @param y
 * @param width
 * @param height
 */
public class Rectangle {
	public enum Anchor {
		LT, CT, RT, LC, CC, RC, LB, CB, RB;
	}

	final double x, y, width, height;

	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/*
	 * Was going to be used to anchor to a certain corner ...
	 * 
	 * public Rectangle (double x, double y, double width, double height,
	 * AnchorX ax, AnchorY ay){
	 * 
	 * switch(ax) { case L: break; case C: x-=(width/2); break; case R: x-=
	 * width; break; } switch(ay) { case T: break; case C: y-=(height/2); break;
	 * case B: y-=height; break; } this.x=x; this.y=y; this.width = width;
	 * this.height = height; }
	 */

	public Point2D.Double getUpperLeft() {
		return new Point2D.Double(x, y);
	}

	public Point2D.Double getOrigin(Anchor anchor) {

		switch (anchor) {
		case LT:
			return new Point2D.Double(x, y);
		case CT:
			return new Point2D.Double(x - (width / 2), y);
		case RT:
			return new Point2D.Double(x - width, y);
			
		case LC:
			return new Point2D.Double(x, y - (height / 2));
		case CC:
			return new Point2D.Double(x - (width /2), y - (height / 2));
		case RC:
			return new Point2D.Double(x - width, y - (height / 2));
			
		case LB:
			return new Point2D.Double(x, y - height);
		case CB:
			return new Point2D.Double(x - (width/2), y - height);
		case RB:
			return new Point2D.Double(x-width, y - height);
			
		}
		throw new AssertionError("Unknown op: " + anchor);
	}
}
