package uk.ac.ox.map.carto.canvas.style;

public class Colour {
	public Colour(String hexColour, float a) {
		setColour(hexColour, a);
	}
	public void setColour(String hexColour, float a) {
		float f = 255;
		this.r = Integer.parseInt(hexColour.substring(1, 3), 16)/ f;
		this.g = Integer.parseInt(hexColour.substring(3, 5), 16)/ f;
		this.b = Integer.parseInt(hexColour.substring(5, 7), 16)/ f;
		this.a = a;
	}
	public void setColour(Colour colour){
		this.r = colour.r;
		this.g = colour.g;
		this.b = colour.b;
		this.a = colour.a;
	}
	public float getRed() {
		return r;
	}
	public float getGreen() {
		return g;
	}
	public float getBlue() {
		return b;
	}
	public float getAlpha() {
		return a;
	}
	
	private float r;
	private float g;
	private float b;
	private float a;

}
