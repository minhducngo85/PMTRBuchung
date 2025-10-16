package vp.tennisbuchung.startup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import vp.tennisbuchung.dtos.Konto;
import vp.tennisbuchung.enums.Halle;
import vp.tennisbuchung.enums.Uhrzeit;
import vp.tennisbuchung.services.BookingService;

import static vp.tennisbuchung.enums.Dauer.ZWEI_STUNDE;
import static vp.tennisbuchung.enums.Tage.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStartupRunner {

    private final BookingService bookingService;

    @PostConstruct
    public void initBooking() {
	log.info("Booking system starting...");
	// Konto kontoVonDuc = new Konto("Ngo", "Duisburg6789");
	// bookingService.scheduleBookingTrigger(HEUTE, Uhrzeit.NEUN, Halle.DUISBURG, 1,
	// ZWEI_STUNDE, kontoVonDuc);

	// Konto kontoVonViet = new Konto("nguyen", "0000");
	// bookingService.scheduleBookingTrigger(MORGEN, Uhrzeit.HALB_ZEHN,
	// Halle.DUISBURG, 3, ZWEI_STUNDE, kontoVonViet);
    }
}
