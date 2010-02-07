package uk.ac.ox.map.carto.canvas;

import java.io.IOException;
import java.util.ArrayList;

import org.freedesktop.cairo.Context;
import org.freedesktop.cairo.Matrix;
import org.freedesktop.cairo.LinearPattern;
import org.freedesktop.cairo.Pattern;
import org.freedesktop.cairo.PdfSurface;
import org.freedesktop.cairo.RadialPattern;
import org.gnome.pango.FontDescription;
import org.gnome.pango.Layout;

import uk.ac.ox.map.carto.server.Admin0;
import uk.ac.ox.map.carto.server.Admin0Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
        
public class CairoTest {
	static PdfSurface pdf;
	static Context cr;
	static Admin0Service a0 = new Admin0Service();
	
	public static void drawMultiPolygon(MultiPolygon mp){
        	Polygon p = (Polygon) mp.getGeometryN(0);
        	for (int i = 1; i == mp.getNumGeometries(); i++) {
        		drawPolygon(p);
			}
	}
	public static void drawPolygon(Polygon p){
        	LineString ls = (LineString) p.getBoundary();
        	for (Coordinate c : ls.getCoordinates()) {
        		System.out.println(c.x);
			}
	}
	
	public static void main(String[] args) throws IOException {
		pdf = new PdfSurface("/tmp/javacairo.pdf", 500, 707);
		
        final Pattern linear, radial;
        cr = new Context(pdf);
        cr.setSource(1.0, 0.1, 0.0, 1.0);
        cr.moveTo(10, 40);
        cr.lineTo(120, 145);
        cr.stroke();
        
    	Matrix m = new Matrix();
    	m.translate(90, 180);
        cr.transform(m);
    	
        ArrayList<Admin0> a = a0.getAdminUnit();
        for (Admin0 admin0 : a) {
        	drawMultiPolygon((MultiPolygon) admin0.getGeometry());
		}
        
        /*
         * If youre used to using RGB triplets, just normalize them to
         * the 0.0 to 1.0 range by dividing by 255. It's all the same
         * to Cairo, really.
         */

        cr.setSource(225 / 255.0, 148 / 255.0, 11 / 255.0, 1.0);
        cr.rectangle(70, 70, 20, 40);
        cr.fill();

        /*
         * Now a much more complicated example of drawing: a linear
         * colour gradiant with a radial alpha mask.
         */

        linear = new LinearPattern(0, 0, 150, 150);
        linear.addColorStopRGB(0.0, 0.0, 0.3, 0.8);
        linear.addColorStopRGB(1.0, 0.0, 0.8, 0.3);

        radial = new RadialPattern(75, 75, 15, 75, 75, 60);
        radial.addColorStopRGBA(0, 0.0, 0.0, 0.0, 0.0);
        radial.addColorStopRGBA(1, 0.0, 0.0, 0.0, 1.0);

        cr.setSource(linear);
        cr.mask(radial); 
        
        cr.setSource(1,1,1,1);
        Layout layout = new Layout(cr);
        layout.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. In fringilla nisi tortor, sed ultricies purus. In hac habitasse platea dictumst. Pellentesque fermentum, nisl vel placerat condimentum, lacus lacus facilisis diam, at rutrum tortor lacus non purus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nunc aliquam posuere erat, vel congue diam lobortis id. In ultricies laoreet vestibulum. Aenean id volutpat ante. Sed sodales lacus et erat dictum scelerisque. Aliquam erat volutpat. Quisque dolor lacus, dignissim nec eleifend quis, egestas ac lectus. Nulla scelerisque, tortor at sagittis auctor, augue elit fringilla lectus, in pretium eros diam sit amet elit. Aliquam erat volutpat. Pellentesque dictum sem ac erat feugiat aliquam. Etiam aliquam porta turpis in sollicitudin. ");
        layout.setWidth(400);
        FontDescription desc = new FontDescription();
        desc.setFamily("Helvetica");
        desc.setSize(20);
		layout.setFontDescription(desc);
        cr.showLayout(layout);
        
        pdf.finish();

	}

}
