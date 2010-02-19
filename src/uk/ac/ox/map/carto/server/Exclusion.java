package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public class Exclusion {
	private Geometry geom;
	private String name;
	private Integer id;
	private Country country;
	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
	public Geometry getGeom() {
		return geom;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	public Country getCountry() {
		return country;
	}

}
