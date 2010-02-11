package uk.ac.ox.map.carto.server;

public class PvAdminUnit extends AdminUnit implements RiskMap {
	private Integer risk;
	private Double avi;

	public void setRisk(Integer risk) {
		this.risk = risk;
	}

	public Integer getRisk() {
		return risk;
	}

	public void setAvi(Double avi) {
		this.avi = avi;
	}

	public Double getAvi() {
		return avi;
	}
}
