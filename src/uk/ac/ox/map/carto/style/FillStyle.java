package uk.ac.ox.map.carto.style;

import org.gnome.gdk.Color;

import uk.ac.ox.map.carto.canvas.style.Colour;

/**
 * @author will
 * 
 *         Incomplete
 */
public class FillStyle {
	
	private final Colour fillColor;
	private final FillType fillType;
	
	public enum FillType {
		SOLID, HATCHED, STIPPLED;
	}

	public FillStyle(Colour colour, FillType fillType) {
		this.fillColor = colour;
		this.fillType = fillType;
    }

	public Colour getFillColor() {
		return fillColor;
	}


	public FillType getFillType() {
	    return fillType;
    }

}