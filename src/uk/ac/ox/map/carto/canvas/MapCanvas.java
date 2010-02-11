package uk.ac.ox.map.carto.canvas;

import org.freedesktop.cairo.PdfSurface;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;
import org.gnome.pango.Style;
import org.gnome.pango.StyleAttribute;

public class MapCanvas extends BaseCanvas {
	
	public MapCanvas(PdfSurface pdf, int width, int height) {
		super(pdf);
		Layout layout = new Layout(cr);
		
		StyleAttribute attr = new StyleAttribute(Style.ITALIC);
        final FontDescription desc;
        desc = new FontDescription("Helvetica, 12");
        layout.setFontDescription(desc);
		layout.setMarkup("<span foreground='blue' size='x-large'>Lorem ipsum sit </span><i>suas denique persequeris et,</i> eam no wisi velit tamquam. Vis ne decore scripserit comprehensam, eros veritus comprehensam vix no. Homero debitis intellegebat sed ex, vim te reque putant pertinax. Et has accusamus prodesset, te clita mentitum prodesset quo, dolorum tibique vis ne. Vidit prodesset consectetuer has in, eam et alii choro fuisset, dicam lucilius necessitatibus cu eos. Quo ne novum fabellas torquatos, sea simul nusquam blandit ea.");
        layout.setWidth(400);
        layout.setJustify(true);
        cr.setSource(0.0, 0.0, 0.0);
        cr.moveTo(20, 500);
        cr.showLayout(layout);

	}
	
	public void drawDataFrame(DataFrame df, int x, int y){
		cr.setSource(df.getSurface(), x, y);
		cr.paint();
	}
	
	public void setLogo(Pixbuf pb, int x, int y){
		cr.scale(0.2, 0.2);
		cr.setSource(pb, x, y);
		cr.paint();
	}
	
	
}
