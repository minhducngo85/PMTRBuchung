package vp.tennisbuchung.enums;

public enum Uhrzeit {
    UHR_0700(0, 7, 00), UHR_0730(1, 7, 30), UHR_0800(2, 8, 00), UHR_0830(3, 8, 30), UHR_0900(4, 9, 00), UHR_0930(5, 9, 30), UHR_1000(6, 10, 00), UHR_1030(7, 10, 30), UHR_1100(8, 11, 00), UHR_1130(9, 11, 30), UHR_1200(10, 12, 00), UHR_1230(11, 12, 30), UHR_1300(12, 13, 00), UHR_1330(13, 13, 30), UHR_1400(14, 14, 00), UHR_1430(15, 14, 30), UHR_1500(16, 15, 00), UHR_1530(17, 15, 30), UHR_1600(18, 16, 00), UHR_1630(19, 16, 30), UHR_1700(20, 17, 00), UHR_1730(21, 17, 30),
    UHR_1800(22, 18, 0), UHR_1830(23, 18, 30), UHR_1900(24, 19, 0), UHR_1930(25, 19, 30), UHR_2000(26, 20, 0),
    UHR_2030(27, 20, 30), UHR_2100(28, 21, 0), UHR_2130(29, 21, 30), UHR_2200(30, 22, 0);

    Uhrzeit(int numOfNextClicks, int hour, int minute) {
	this.numOfNextClicks = numOfNextClicks;
	this.hour = hour;
	this.minute = minute;
    }

    private final Integer numOfNextClicks;
    private final Integer hour;
    private final Integer minute;

    /**
     * @return the numOfNextClicks
     */
    public Integer getNumOfNextClicks() {
	return numOfNextClicks;
    }

    /**
     * @return the hour
     */
    public Integer getHour() {
	return hour;
    }

    /**
     * @return the minute
     */
    public Integer getMinute() {
	return minute;
    }

    public String getStringValue() {
	String minute = "00";
	if (getMinute() != 0) {
	    minute = getMinute().toString();
	}
	return getHour() + ":" + minute;
    }

    public static Uhrzeit getUhrZeit(String name) {
	String[] splited = name.split(":");
	if (splited.length != 2) {
	    return null;
	}
	try {
	    int hour = Integer.valueOf(splited[0]);
	    int minute = Integer.valueOf(splited[1]);
	    // iterate over enums using for loop
	    for (Uhrzeit s : Uhrzeit.values()) {
		if (s.getHour().intValue() == hour && s.getMinute().intValue() == minute) {
		    return s;
		}
	    }
	} catch (NumberFormatException e) {
	}
	return null;
    }

}
