package uk.ac.ox.map.carto.canvas;

import uk.ac.ox.map.domain.carto.Colour;

public class MapKeyItem {
	public enum FillStyle {
		SOLID, HATCHED, STIPPLED;
	}

	public final String description;
	public final Colour colour;
	public boolean hatched = false;
	public boolean stippled = false;
	public FillStyle fillStyle;
	public boolean duffy = false;
	public boolean point = false;

	public MapKeyItem(String description, Colour colour) {
		this.description = description;
		this.colour = colour;
	}
}