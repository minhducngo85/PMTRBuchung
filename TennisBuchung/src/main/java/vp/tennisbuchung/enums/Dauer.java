package vp.tennisbuchung.enums;

public enum Dauer {
    EINE_STUNDE(0),
    EINE_STUNDE_30_MINUTE(1),
    ZWEI_STUNDE(2),
    ZWEI_STUNDE_30_MINUTE(3);

    Dauer(int i) {
    	this.value = i;
    }

	public Integer getValue() {
		return value;
	}

	private final Integer value;
}
