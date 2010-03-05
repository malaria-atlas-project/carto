package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public class AdminUnitRisk implements RiskMap {
	private String countryId;
	private Integer risk;
	private Double api;
	private String parasite;
	
	private Geometry geom;
	private String name;
	private Integer id;
	private String adminLevel;

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

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setAdminLevel(String adminLevel) {
		this.adminLevel = adminLevel;
	}

	public String getAdminLevel() {
		return adminLevel;
	}

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setApi(Double api) {
		this.api = api;
	}

	public Double getApi() {
		return api;
	}

	public void setParasite(String parasite) {
		this.parasite = parasite;
	}

	public String getParasite() {
		return parasite;
	}
}
