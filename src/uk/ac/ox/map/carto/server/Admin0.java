package uk.ac.ox.map.carto.server;
import com.vividsolutions.jts.geom.Geometry;


public class Admin0 {
	private Geometry geometry;
	private String name;
	private Integer id;
	private String countryId;

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public Geometry getGeometry() {
		return geometry;
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

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountryId() {
		return countryId;
	}
}
