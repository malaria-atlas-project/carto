package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Surface;
import org.gnome.rsvg.Handle;

public class SvgPattern {
	
	protected final Context cr;
	protected final Surface surface;
	
	public SvgPattern(Surface s, Handle h) {
		this.surface = s;
		this.cr = new Context(s);
	}

	public void drawSVG(Rectangle frame, Handle h) {
	    cr.save();
	    
	    double sx = frame.width / h.getDimensions().getWidth();
	    double sy = frame.height / h.getDimensions().getHeight();
	    
	    cr.scale(sx, sy);
	    cr.translate(frame.x/sx, frame.y/sy);
	    cr.showHandle(h);
	    
	    cr.restore();
	  }
	

}
