package com.search.project.yelp;

public class BusinessHours {
	public BusinessDay Sunday;
	public BusinessDay Monday;
	public BusinessDay Tuesday;
	public BusinessDay Wednesday;
	public BusinessDay Thursday;
	public BusinessDay Friday;
	public BusinessDay Saturday;

	public BusinessDay getSunday() {
		return Sunday;
	}

	public void setSunday(BusinessDay sunday) {
		Sunday = sunday;
	}

	public BusinessDay getMonday() {
		return Monday;
	}

	public void setMonday(BusinessDay monday) {
		Monday = monday;
	}

	public BusinessDay getTuesday() {
		return Tuesday;
	}

	public void setTuesday(BusinessDay tuesday) {
		Tuesday = tuesday;
	}

	public BusinessDay getWednesday() {
		return Wednesday;
	}

	public void setWednesday(BusinessDay wednesday) {
		Wednesday = wednesday;
	}

	public BusinessDay getThursday() {
		return Thursday;
	}

	public void setThursday(BusinessDay thursday) {
		Thursday = thursday;
	}

	public BusinessDay getFriday() {
		return Friday;
	}

	public void setFriday(BusinessDay friday) {
		Friday = friday;
	}

	public BusinessDay getSaturday() {
		return Saturday;
	}

	public void setSaturday(BusinessDay saturday) {
		Saturday = saturday;
	}

	@Override
	public String toString() {
		return "BusinessHours [Sunday=" + Sunday + ", Monday=" + Monday
				+ ", Tuesday=" + Tuesday + ", Wednesday=" + Wednesday
				+ ", Thursday=" + Thursday + ", Friday=" + Friday
				+ ", Saturday=" + Saturday + "]";
	}

}
