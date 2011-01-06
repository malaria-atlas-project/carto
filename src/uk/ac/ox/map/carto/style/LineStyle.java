package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.deps.Colour;



public class LineStyle {
	private final double lineWidth;
	private final Colour lineColour;
	
	public LineStyle(Colour lineColour, double lineWidth) {
		this.lineColour = lineColour;
		this.lineWidth = lineWidth;
    }

	public double getLineWidth() {
		return lineWidth;
	}

	public Colour getLineColour() {
		return lineColour;
	}
}
