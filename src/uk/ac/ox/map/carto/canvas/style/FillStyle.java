package uk.ac.ox.map.carto.canvas.style;

public class FillStyle  {
	
	private Colour fillColour;

	public void setFillColour(String hex, float alpha) {
		this.fillColour = new Colour(hex, alpha);
	}

	public Colour getFillColour() {
		return fillColour;
	}

}