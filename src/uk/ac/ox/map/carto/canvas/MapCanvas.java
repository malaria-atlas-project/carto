package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;
import org.gnome.pango.Style;
import org.gnome.pango.StyleAttribute;

import uk.ac.ox.map.carto.canvas.ScaleBar.SegmentType;

public class MapCanvas extends BaseCanvas {
	
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf, width, height);

	}
	public void addTitle(String title) {
		
		Layout layout = new Layout(cr);
        FontDescription fontDesc = new FontDescription("Helvetica, 10");
        layout.setFontDescription(fontDesc);
		layout.setMarkup("<span foreground='blue' size='x-large'>Lorem ipsum sit </span><i>suas denique persequeris et,</i> eam no wisi velit tamquam. Vis ne decore scripserit comprehensam, eros veritus comprehensam vix no. Homero debitis intellegebat sed ex, vim te reque putant pertinax. Et has accusamus prodesset, te clita mentitum prodesset quo, dolorum tibique vis ne. Vidit prodesset consectetuer has in, eam et alii choro fuisset, dicam lucilius necessitatibus cu eos. Quo ne novum fabellas torquatos, sea simul nusquam blandit ea.");
        layout.setWidth(width);
        layout.setJustify(true);
        cr.setSource(0.0, 0.0, 0.0);
        cr.moveTo(20, 500);
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
		ScaleBar sb = new ScaleBar(cr, frame, scale);
	}
	
	public enum SegmentType {HOLLOW, FILLED}
	
	public class ScaleBar {
		private static final double KM_PER_DEGREE = 111.320;
		private final int barHeight;
		private final double scale;
		private int offset;
		private Context cr;
		private Frame frame;


		public ScaleBar(Context cr, Frame frame, double scale) {
			MapCanvas outer = MapCanvas.this;
			this.cr = outer.cr;
			//TODO: hard code
			this.barHeight = 5;
			this.frame = frame;
			
			this.scale = scale;
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
	        int interval;
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
	        	
	        	drawSegment((int) divisionWidth, st);
			}


		}
		
	    private void drawSegment(int width, SegmentType st){
	        cr.rectangle(offset, frame.y, width, barHeight);
	        if (st == SegmentType.FILLED) {
		        cr.fillPreserve();
	        }
	        cr.stroke();
	        offset += width;
	    }


	}
	
}
