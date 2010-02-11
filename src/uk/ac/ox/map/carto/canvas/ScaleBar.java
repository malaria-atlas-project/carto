package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.Context;

public class ScaleBar {
	private static final double KM_PER_DEGREE = 111.320;
	private final int barHeight;
	private final double scale;
	private int offset;
	public enum SegmentType {HOLLOW, FILLED}
	private Context cr;
	private Frame frame;


	public ScaleBar(Context cr, Frame frame, double scale) {
		this.cr = cr;
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
