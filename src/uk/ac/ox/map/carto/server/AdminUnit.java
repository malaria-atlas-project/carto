package uk.ac.ox.map.carto.server;
import com.vividsolutions.jts.geom.Geometry;


public class AdminUnit {
	private Geometry geom;
	private String name;
	private Integer id;
	private String countryId;
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

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountryId() {
		return countryId;
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
}
