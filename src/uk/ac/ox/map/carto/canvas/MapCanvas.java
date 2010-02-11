package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;

public class MapCanvas extends BaseCanvas {
	
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf, width, height);

	}
	
	private MapCanvas outer(){
		return this;
	}
	
	public void annotateMap(String text, int x, int y, AnchorX ax, AnchorY ay) {
		
		Layout layout = new Layout(cr);
        FontDescription fontDesc = new FontDescription("Helvetica, 8");
        layout.setFontDescription(fontDesc);
		layout.setMarkup(text);
		layout.getPixelWidth();
		
        cr.setSource(0.0, 0.0, 0.0);
        cr.moveTo(ax.eval(layout.getPixelWidth(), x), ay.eval(layout.getPixelHeight(), y));
        cr.showLayout(layout);
		
	}
	
	public void drawDataFrame(DataFrame df, int x, int y){
		cr.setSource(df.getSurface(), x, y);
		cr.paint();
//		cr.rectangle(x, y, df.getWidth(), df.getHeight());
//		cr.stroke();
	}
	
	public void setLogo(Pixbuf pb, int x, int y){
		cr.scale(0.2, 0.2);
		cr.setSource(pb, x, y);
		cr.paint();
		cr.scale(5, 5);
	}

	public void setScaleBar(Frame frame, double scale){
		ScaleBar sb = new ScaleBar(frame, scale);
	}
	
	public enum SegmentType {HOLLOW, FILLED}
	
	public enum AnchorX {
		L, C, R; 
		int eval(int w, int x){
	        switch(this) {
	            case L: return x;
	            case C: return x-(w/2);
	            case R: return x-w;
			}
            throw new AssertionError("Unknown op: " + this);
		}
	}
		
	public enum AnchorY {
		T, C, B;
		int eval(int h, int y){
	        switch(this) {
	            case T: return y;
	            case C: return y-(h/2);
	            case B: return y-h;
			}
            throw new AssertionError("Unknown op: " + this);
		}
	}
	
	public class ScaleBar {
		private static final double KM_PER_DEGREE = 111.320;
		private final int barHeight;
		private int offset;
		private Context cr;
		private Frame frame;

		public ScaleBar(Frame frame, double scale) {
			this.cr = outer().cr;
			//TODO: hard code
			this.barHeight = 5;
			this.frame = frame;
			this.offset = frame.x;
			
			cr.setLineWidth(0.2);
	        cr.setSource(0, 0, 0, 1);
			/*
			 * Find the max space available
			 */
	        double maxDist = (frame.width / scale) * KM_PER_DEGREE;
	        int intervals[] = {10000, 5000, 2500, 2000, 1000, 500, 250, 100, 50, 25, 10};
	        
	        double divisionWidth = 0;
	        double nIntervals = 0;
	        int interval = 0;
	        for (int i = 0; i < intervals.length; i++) {
				interval = intervals[i];
	            nIntervals = maxDist / interval;
	            if (nIntervals > 2) {
	                divisionWidth = (interval / KM_PER_DEGREE) * scale;
	                break;
	            }
			}

	        if (!(nIntervals >= 2)) {
	        	System.err.println("not enough intervals");
	        }
	        
	        int n = (int) nIntervals;
	        SegmentType st;
	        for (int i = 0; i < n; i++) {
	        	if((i%2) == 0)
		            st = SegmentType.HOLLOW;
	        	else
		            st = SegmentType.FILLED;       		
	        	
	        	drawSegment((int) divisionWidth, st, ""+(interval * (i+1)));
			}
		}
		
	    private void drawSegment(int width, SegmentType st, String text){
	        cr.rectangle(offset, frame.y, width, barHeight);
	        if (st == SegmentType.FILLED) {
		        cr.fillPreserve();
	        }
	        cr.stroke();
	        offset += width;
        	outer().annotateMap(text, offset, frame.y, AnchorX.C, AnchorY.B);
	    }
	}
}
