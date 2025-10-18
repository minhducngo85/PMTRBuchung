package vp.tennisbuchung.enums;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum Tage {
    HEUTE(0, 0), 
    MORGEN(1, 1), 
    UEBERMORGEN(2, 2),
    NACHSTE_3_TAGE(3, 3), 
    NACHSTE_4_TAGE(4, 4),
    NACHSTE_5_TAGE(5, 5), 
    NACHSTE_6_TAGE(6, 6),
    NACHSTE_7_TAGE(7, 7);

    private final Integer numOfNextClicks;
    private final int plusDays;

    Tage(int i, int plusDays) {
	this.numOfNextClicks = i;
	this.plusDays = plusDays;
    }

    public Integer getNumOfNextClicks() {
	return numOfNextClicks;
    }

    public LocalDate getBookingDate() {
	return LocalDate.now().plusDays(plusDays);
    }

    public String getName() {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	String formattedString = getBookingDate().format(formatter);
	return formattedString;
    }
}
