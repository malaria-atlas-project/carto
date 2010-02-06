package uk.ac.ox.map.carto.canvas;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

public class TestSVGGenerator {

    public void paint(Graphics2D g2d) {
        g2d.setPaint(Color.red);
        g2d.fill(new Rectangle(10, 10, 100, 100));
        Polygon p = new Polygon();
        //g2d.drawPolygon(p);
    }

    public static void main(String [] args) throws IOException {
        // Get a DOMImplementation
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";

        // Create an instance of org.w3c.dom.Document
        Document document = 
            domImpl.createDocument(svgNamespaceURI, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Render into the SVG Graphics2D implementation
        TestSVGGenerator test = new TestSVGGenerator();
        test.paint(svgGenerator);

        // Finally, stream out SVG to the standard output using UTF-8
        // character to byte encoding
        boolean useCSS = true; // we want to use CSS style attribute
        Writer out = new OutputStreamWriter(System.out, "UTF-8");
        svgGenerator.stream(out, useCSS);
    }
}

