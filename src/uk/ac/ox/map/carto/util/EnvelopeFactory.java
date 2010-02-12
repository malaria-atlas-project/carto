package uk.ac.ox.map.carto.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class EnvelopeFactory {
	public static Envelope envelopeFromPolygon(Polygon poly, double expandRatio){
        	/*
        	 * Why doesn't geometry return an envelope? This envelope creation shouldn't be necessary
        	 */
    		Envelope env = new Envelope();
        	for (Coordinate c: poly.getCoordinates()) {
				env.expandToInclude(c);
			}
        	double deltaX = (env.getWidth() * expandRatio) - env.getWidth();
			double deltaY = (env.getHeight() * expandRatio) - env.getHeight();
			env.expandBy(deltaX/2, deltaY/2);
			return env;
		
	}
	
}