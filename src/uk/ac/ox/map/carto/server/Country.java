package uk.ac.ox.map.carto.server;

import com.vividsolutions.jts.geom.Geometry;

public class Country {
	private String id;
	private String name;
	private Geometry envelope;
	private Boolean pfEndemic;
	private Boolean pvEndemic;


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

	public void setEnvelope(Geometry envelope) {
		this.envelope = envelope;
	}

	public Geometry getEnvelope() {
		return envelope;
	}

	public void setPfEndemic(Boolean pfEndemic) {
		this.pfEndemic = pfEndemic;
	}

	public Boolean getPfEndemic() {
		return pfEndemic;
	}

	public void setPvEndemic(Boolean pvEndemic) {
		this.pvEndemic = pvEndemic;
	}

	public Boolean getPvEndemic() {
		return pvEndemic;
	}

}
