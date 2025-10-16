package vp.tennisbuchung.enums;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public enum Tage {
    HEUTE(0, LocalDate.now()),
    MORGEN(1, LocalDate.now().plusDays(1)),
    UEBERMORGEN(2, LocalDate.now().plusDays(2)),
    NACHSTE_3_TAGE(3, LocalDate.now().plusDays(3)),
    NACHSTE_4_TAGE(4, LocalDate.now().plusDays(4)),
    NACHSTE_5_TAGE(5, LocalDate.now().plusDays(5)),
    NACHSTE_6_TAGE(6, LocalDate.now().plusDays(6)),
    NACHSTE_7_TAGE(7, LocalDate.now().plusDays(7));

    Tage(int i, LocalDate plusDays) {
    	this.numOfNextClicks = i;
    	this.bookingDate = plusDays;
	}
	public Integer getNumOfNextClicks() {
		return numOfNextClicks;
	}
	public LocalDate getBookingDate() {
		return bookingDate;
	}
	private final Integer numOfNextClicks;
    private final LocalDate bookingDate;
    
    public String getName() {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    	String formattedString = bookingDate.format(formatter);
    	return formattedString;
    }
}
