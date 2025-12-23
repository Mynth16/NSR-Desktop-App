package application.models;

public class Resident {
		private String residentId;
		private String name;
		private int age;
		private String gender;
		private String civilStatus;
		private String household;
		private String contact;
		private String birthDate; // YYYY-MM-DD
		private String educationalAttainment;
		private boolean registeredVoter;
		private boolean pwd;
		private String status;

	public Resident(String residentId, String name, int age, String gender, String civilStatus, String household, String contact, String birthDate, String educationalAttainment, boolean registeredVoter, boolean pwd, String status) {
		this.residentId = residentId;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.civilStatus = civilStatus;
		this.household = household;
		this.contact = contact;
		this.birthDate = birthDate;
		this.educationalAttainment = educationalAttainment;
		this.registeredVoter = registeredVoter;
		this.pwd = pwd;
		this.status = status;
	}

	public String getResidentId() { return residentId; }
	public String getName() { return name; }
	public int getAge() { return age; }
	public String getGender() { return gender; }
	public String getCivilStatus() { return civilStatus; }
	public String getStatus() { return status; }
	public String getHousehold() { return household; }
	public String getContact() { return contact; }
	public String getBirthDate() { return birthDate; }
	public String getEducationalAttainment() { return educationalAttainment; }
	public String getRegisteredVoter() { return registeredVoter ? "Yes" : "No"; }
	public boolean isRegisteredVoter() { return registeredVoter; }
	public boolean isPwd() { return pwd; }
}
