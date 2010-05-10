package uk.ac.ox.map.carto.style;

public class Colour {
	
	public Colour(String hexColour, float a) {
		setColour(hexColour, a);
	}
	
	public Colour (double r, double g, double b, double a){
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public void setColour(String hexColour, double a) {
		float f = 255;
		this.r = Integer.parseInt(hexColour.substring(1, 3), 16)/ f;
		this.g = Integer.parseInt(hexColour.substring(3, 5), 16)/ f;
		this.b = Integer.parseInt(hexColour.substring(5, 7), 16)/ f;
		this.a = a;
	}
	public double getRed() {
		return r;
	}
	public double getGreen() {
		return g;
	}
	public double getBlue() {
		return b;
	}
	public double getAlpha() {
		return a;
	}
	
	private double r;
	private double g;
	private double b;
	private double a;

}
