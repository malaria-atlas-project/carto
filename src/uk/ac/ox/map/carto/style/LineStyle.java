package uk.ac.ox.map.carto.style;

import uk.ac.ox.map.carto.canvas.style.Colour;


public class LineStyle {
	private double lineWidth;
	private Colour lineColour;

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineColour(String hex, float alpha) {
		lineColour = new Colour(hex, alpha);
	}

	public Colour getLineColour() {
		return lineColour;
	}
}
