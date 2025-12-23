package application.models;

public class Household {
	private String zone;
	private String houseNumber;
	private String headOfHousehold;
	private int residentsCount;
	private String status;

	public Household(String zone, String houseNumber, String headOfHousehold, int residentsCount, String status) {
		this.zone = zone;
		this.houseNumber = houseNumber;
		this.headOfHousehold = headOfHousehold;
		this.residentsCount = residentsCount;
		this.status = status;
	}

	public String getZone() {
		return zone;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public String getHeadOfHousehold() {
		return headOfHousehold;
	}

	public int getResidentsCount() {
		return residentsCount;
	}

	public String getStatus() {
		return status;
	}
}
