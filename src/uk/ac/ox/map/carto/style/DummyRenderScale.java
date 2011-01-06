package uk.ac.ox.map.carto.style;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ox.map.deps.Colour;
import uk.ac.ox.map.imageio.RenderScale;

public class DummyRenderScale implements RenderScale {
	Map<Integer, Colour> colours = new HashMap<Integer, Colour>();
	
    public DummyRenderScale() {
	}
    
    public void putColour(int val, Colour colour) {
		colours.put(val, colour);
    }

	@Override
	public double getBlue(float f) {
		return colours.get((int)f).getBlue();
	}

	@Override
	public double getGreen(float f) {
		return colours.get((int)f).getGreen();
	}

	@Override
	public double getRed(float f) {
		return colours.get((int)f).getRed();
	}

	@Override
	public double getAlpha(float f) {
		return colours.get((int)f).getAlpha();
	}
}
