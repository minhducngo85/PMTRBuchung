package vp.tennisbuchung.enums;

public enum Halle {
    DUISBURG("Duisburg_Tennishalle"), MUELHEIM("Muelheim_Tennishalle");

    private final String name;

    private Halle(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

}
