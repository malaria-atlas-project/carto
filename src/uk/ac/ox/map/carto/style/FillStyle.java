package uk.ac.ox.map.carto.style;

import org.gnome.gdk.Color;

import uk.ac.ox.map.carto.canvas.style.Colour;

/**
 * @author will
 * 
 *         Incomplete
 */
public class FillStyle {
	public enum FillType {
		SOLID, HATCHED, STIPPLED;
	}

	private Colour fillColor;

	public void setFillColor(Colour fillColor) {
		this.fillColor = fillColor;
	}

	public Colour getFillColor() {
		return fillColor;
	}

}