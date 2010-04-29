package uk.ac.ox.map.carto.canvas.style;

/**
 * @author will
 *
 * Incomplete
 */
public class FillStyle  {
	
	private Colour fillColour;

	public void setFillColour(String hex, float alpha) {
		this.fillColour = new Colour(hex, alpha);
	}

	public Colour getFillColour() {
		return fillColour;
	}

}
