package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public class WaterBody {
	private Integer id;
	private Double area;
	private Geometry geom;
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
	public void setArea(Double area) {
		this.area = area;
	}
	public Double getArea() {
		return area;
	}
	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
	public Geometry getGeom() {
		return geom;
	}
}
