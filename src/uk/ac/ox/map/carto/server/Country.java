package uk.ac.ox.map.carto.server;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

public class Country {
	private String id;
	private String name;
	private Geometry geom;

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

	private Set<Integer> years;
	private Set<String> zeroed;
	private String adminLevel;
	private Integer count;
	
	public void setYears(Set<Integer> years) {
		this.years = years;
	}

	public Set<Integer> getYears() {
		return years;
	}
	
	public void setZeroed(Set<String> zeroed) {
		this.zeroed = zeroed;
	}

	public Set<String> getZeroed() {
		return zeroed;
	}

	public void setAdminLevel(String adminLevel) {
		this.adminLevel = adminLevel;
	}

	public String getAdminLevel() {
		return adminLevel;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCount() {
		return count;
	}
}
