package uk.ac.ox.map.carto.canvas;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;

import uk.ac.ox.map.carto.util.AnnotationFactory;

import com.vividsolutions.jts.geom.Envelope;

public class MapCanvas extends BaseCanvas {
	
	private final HashMap<DataFrame, Point> dataFrames = new HashMap<DataFrame, Point>();
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf, width, height);
	}
	
	private MapCanvas outer(){
		return this;
	}
	
	public void annotateMap(String text, int x, int y, AnchorX ax, AnchorY ay, Integer fontSize) {
		
		Layout layout = new Layout(cr);
        FontDescription fontDesc = new FontDescription();
        fontDesc.setFamily("Helvetica");
        fontDesc.setSize(fontSize);
        layout.setFontDescription(fontDesc);
		layout.setMarkup(text);
		layout.getPixelWidth();
		
		//Always default to black. Good idea?
        cr.setSource(0.0, 0.0, 0.0);
        cr.moveTo(ax.eval(layout.getPixelWidth(), x), ay.eval(layout.getPixelHeight(), y));
        cr.showLayout(layout);
		
	}
	
	public void drawDataFrame(DataFrame df, int x, int y){
		cr.setSource(df.getSurface(), x, y);
		cr.paint();
		dataFrames.put(df, new Point(x, y));
	}
	public void drawMapBorders() {
		for (DataFrame df : dataFrames.keySet()) {
			Point offset = dataFrames.get(df);
			cr.rectangle(offset.x, offset.y, df.getWidth(), df.getHeight());
			cr.stroke();
		}
	}
	
	public void drawMapGrids(int fontSize) {
		/*
		 * TODO fix hacking - hardcoding, over complexity; 
		 */
        int intervals[] = {40, 20, 10, 5, 2, 1};
        
		for (DataFrame df : dataFrames.keySet()) {
			Envelope env = df.getEnvelope();
			Point offset = dataFrames.get(df);
	        double dX = env.getWidth();
	        double dY = env.getHeight();
	
	        int chosen_interval = 0;
	        HashMap<Integer, Integer> possible_intervals = new HashMap<Integer, Integer>();
	        
	        for (int i = 0; i < intervals.length; i++) {
	        	/*
	        	 * Floor the max and ceiling the min to get the position of the lower and upper grid lines
	        	 * Same logic for both axes
	        	 * 
	        	 * TODO: all very brittle, no checking, hardcoding, ... in fact not quite sure how it works.
	        	 * 
	        	 */
	        	int I = intervals[i];
	            int nintervalX = (int) (Math.floor(env.getMaxX()/I) - Math.ceil(env.getMinX()/I));
	            int nintervalY = (int) (Math.floor(env.getMaxY()/I) - Math.ceil(env.getMinY()/I));
	            int n = Math.min(nintervalX, nintervalY);
	            if (n > 1 && n < 7) {
	            	possible_intervals.put(n, I);
	            }
			}
	        // This unreadable stuff gets the value with the minimum key
            chosen_interval = possible_intervals.get(Collections.min(possible_intervals.keySet()));
            
	        int x, x1, x2; 
	        int y, y1, y2; 
	        int tic_length = 5;
	        String anno;
            for (int i = -180; i <= 180; i+=chosen_interval) {
            	// Dual purpose: does a meridian and parallel
            	Point2D.Double mapPt = df.transform(i, i);
            /*
             * 	
             */
            	if (i > env.getMinX() && i < env.getMaxX()) {
            		x = (int) (offset.x + mapPt.x);
	                y1 = offset.y - tic_length;
	                y2 = offset.y + df.getHeight() + tic_length;
	
	            	//meridian
	            	cr.moveTo(x, y1);
	            	cr.lineTo(x, y2);
	            	cr.stroke();
	            	anno = AnnotationFactory.gridText(i, "EW");
	            	annotateMap(anno, x, y1, AnchorX.C, AnchorY.B, fontSize);
	            	annotateMap(anno, x, y2, AnchorX.C, AnchorY.T, fontSize);
            	}
            	
            	if (i > env.getMinY() && i < env.getMaxY()) {
            		y = (int) (offset.y + mapPt.y);
	                x1 = offset.x - tic_length;
	                x2 = offset.x + df.getWidth() + tic_length;
	
	            	//meridian
	            	cr.moveTo(x1, y);
	            	cr.lineTo(x2, y);
	            	cr.stroke();
	            	anno = AnnotationFactory.gridText(i, "SN");
	            	annotateMap(anno, x1, y, AnchorX.R, AnchorY.C, fontSize);
	            	annotateMap(anno, x2, y, AnchorX.L, AnchorY.C, fontSize);
            	}
            	
			}
	        
		}     
        
	}
	
	public void setLogo(Pixbuf pb, int x, int y){
		cr.scale(0.2, 0.2);
		cr.setSource(pb, x, y);
		cr.paint();
		cr.scale(5, 5);
	}

	public void setScaleBar(Frame frame, double scale, int fontSize){
		ScaleBar sb = new ScaleBar(frame, scale, fontSize);
	}
	
	public enum SegmentType {HOLLOW, FILLED}
	
	/*
	 * TODO prettify
	 */
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
	
	/*
	 * TODO: should this be an inner class?
	 */
	public class ScaleBar {
		private static final double KM_PER_DEGREE = 111.320;
		private final int barHeight;
		private int offset;
		private Context cr;
		private Frame frame;
		private final int fontSize;

		public ScaleBar(Frame frame, double scale, int fontSize) {
			this.cr = outer().cr;
			this.fontSize = fontSize;
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
	        
        	outer().annotateMap("0", offset, frame.y, AnchorX.C, AnchorY.B, fontSize);
        	
	        for (int i = 0; i < n; i++) {
	        	if((i%2) == 0)
		            st = SegmentType.HOLLOW;
	        	else
		            st = SegmentType.FILLED;       		
	        	
	        	drawSegment((int) divisionWidth, st, ""+(interval * (i+1)));
			}
	        
        	outer().annotateMap("Kilometres", offset+10, frame.y, AnchorX.L, AnchorY.T, fontSize);
		}
		
	    private void drawSegment(int width, SegmentType st, String text){
	        cr.rectangle(offset, frame.y, width, barHeight);
	        if (st == SegmentType.FILLED) {
		        cr.fillPreserve();
	        }
	        cr.stroke();
	        offset += width;
        	outer().annotateMap(text, offset, frame.y, AnchorX.C, AnchorY.B, fontSize);
	    }
	}
}
