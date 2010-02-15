package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public class Country {
	private String id;
	private String name;
	private Geometry geom;
	private Boolean hasPf;
	private Boolean hasPv;

	public void setGeom(Geometry geometry) {
		this.geom = geometry;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setHasPf(Boolean hasPf) {
		this.hasPf = hasPf;
	}

	public Boolean getHasPf() {
		return hasPf;
	}

	public void setHasPv(Boolean hasPv) {
		this.hasPv = hasPv;
	}

	public Boolean getHasPv() {
		return hasPv;
	}

}
