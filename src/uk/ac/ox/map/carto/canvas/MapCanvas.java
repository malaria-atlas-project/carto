package uk.ac.ox.map.carto.canvas;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.Alignment;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;

import uk.ac.ox.map.carto.util.AnnotationFactory;


import com.vividsolutions.jts.geom.Envelope;

public class MapCanvas extends BaseCanvas {
	
	private final HashMap<DataFrame, Point> dataFrames = new HashMap<DataFrame, Point>();
	private final FontDescription fontDesc;
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf, width, height);
        fontDesc = new FontDescription();
        fontDesc.setFamily("Helvetica");
        fontDesc.setSize(12);
	}
	
	private MapCanvas outer(){
		return this;
	}
	
	public void setTitle(String text, Rectangle frame, Integer fontSize){
		/*
		 * TODO look at Reportlab api to think of good ways of abstracting
		 */
		Layout layout = new Layout(cr);
        fontDesc.setSize(fontSize);
        layout.setFontDescription(fontDesc);
        layout.setWidth(frame.width);
        layout.setAlignment(Alignment.CENTER);
		layout.setMarkup(text);
		
		//Always default to black. Good idea?
        cr.setSource(0.0, 0.0, 0.0);
        cr.moveTo(frame.x, frame.y);
        cr.showLayout(layout);
	}
	
	public void drawTextFrame(List<String> text, Rectangle frame, float fontSize){
		Layout layout = new Layout(cr);
        fontDesc.setSize(fontSize);
        layout.setFontDescription(fontDesc);
        layout.setWidth(frame.width);
        layout.setJustify(true);
        
        cr.setSource(0.0, 0.0, 0.0);
        
        double y = frame.y; 
        
		for (String string : text) {
	        cr.moveTo(frame.x, y);
			layout.setMarkup(string);
	        cr.showLayout(layout);
	        y += layout.getPixelHeight() + 10;
		}
		
	}
	
	public void annotateMap(List<String> anno, double x, double y, AnchorX ax, AnchorY ay) {
		for (String string : anno) {
			annotateMap(string, x, y, ax, ay);
		}
	} 
	
	public void annotateMap(String text, double x, double y, AnchorX ax, AnchorY ay) {
		
		Layout layout = new Layout(cr);
        layout.setFontDescription(fontDesc);
		layout.setMarkup(text);
		
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
	public void drawLegend(Rectangle rect, List<LegendItem> legend){
		double y = rect.y;
		double x = rect.x;
		
		/*
		 * Could be configuration options?
		 */
		double patchWidth = 25;
		double patchHeight = 12.5;
		double spacing = 22;
		double textMargin = 40;
		
		fontDesc.setSize(6);
		for (LegendItem li : legend) {
			setFillColour(li.colour);
			cr.rectangle(x, y, patchWidth, patchHeight);
			cr.fillPreserve();
			setLineColour();
			if (li.hatched) {
				//don't clip preserve
				cr.save();
		        cr.clipPreserve();
		        paintCrossHatch();
		        cr.restore();
			}
			else if (li.stippled) {
				//don't clip preserve
				cr.save();
		        cr.clipPreserve();
		        paintStipple();
		        cr.restore();
			}
			setLineColour();
			cr.setDash(new double[] {1,0});
			cr.stroke();
			
			annotateMap(li.description, x+textMargin, y + (patchHeight/2), AnchorX.L, AnchorY.C);
			y += spacing;
		}
	}
	
	
	public void drawMapGrids(double fontSize) {
		/*
		 * TODO fix hacking - hardcoding, over complexity; 
		 * Pass in interval?? More of a callers job really.
		 */
        int intervals[] = {40, 20, 10, 5, 2, 1};
        fontDesc.setSize(fontSize);
        
		for (DataFrame df : dataFrames.keySet()) {
			Envelope env = df.getEnvelope();
			Point offset = dataFrames.get(df);
	
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
	        try {
	            chosen_interval = possible_intervals.get(Collections.min(possible_intervals.keySet()));
			} catch (Exception e) {
				System.err.println(e.getMessage());
				chosen_interval = 1;
			}
            
	        int x, x1, x2; 
	        int y, y1, y2; 
	        int tic_length = 3;
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
	            	annotateMap(anno, x, y1, AnchorX.C, AnchorY.B);
	            	annotateMap(anno, x, y2, AnchorX.C, AnchorY.T);
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
	            	annotateMap(anno, x1, y, AnchorX.R, AnchorY.C);
	            	annotateMap(anno, x2, y, AnchorX.L, AnchorY.C);
            	}
            	
			}
	        
		}     
        
	}
	
	public void setLogo(Pixbuf pb, Rectangle frame){
		System.out.println("W:" + pb.getWidth());
		System.out.println("H:" + pb.getHeight());
		double scale = pb.getWidth() / frame.width;
		System.out.println("Scale:" + scale);
		cr.scale(scale, scale);
		cr.setSource(pb, 0,0);
		cr.paint();
	}

	public void setScaleBar(Rectangle frame, double scale, int fontSize){
		ScaleBar sb = new ScaleBar(frame, scale, fontSize);
	}
	
	public enum SegmentType {HOLLOW, FILLED}
	
	/*
	 * TODO prettify
	 */
	public enum AnchorX {
		L, C, R; 
		double eval(int w, double x){
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
		double eval(int h, double y){
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
	 * TODO: should it be a class at all?? No state is required
	 */
	public class ScaleBar {
		private static final double KM_PER_DEGREE = 111.320;
		private final int barHeight;
		private double offset;
		private Context cr;
		private Rectangle frame;
	    private NumberFormat formatter = new DecimalFormat("###,###,###");

		public ScaleBar(Rectangle frame, double scale, int fontSize) {
			this.cr = outer().cr;
			
			//TODO: hard code
			this.barHeight = 5;
			this.frame = frame;
			this.offset = frame.x;
			
			//Set the font size
			outer().fontDesc.setSize(fontSize);
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
	        
        	outer().annotateMap("0", offset, frame.y, AnchorX.C, AnchorY.B);
        	
	        for (int i = 0; i < n; i++) {
	        	if((i%2) == 0)
		            st = SegmentType.HOLLOW;
	        	else
		            st = SegmentType.FILLED;       		
	        	
	        	drawSegment((int) divisionWidth, st, formatter.format(interval * (i+1)));
			}
	        
        	outer().annotateMap("Kilometres", offset+10, frame.y, AnchorX.L, AnchorY.T);
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
