package vp.tennisbuchung.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vp.tennisbuchung.services.BookingService;

@RestController
@RequestMapping(value = "/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    ResponseEntity<String> book(@RequestParam Integer day, @RequestParam Integer time, @RequestParam Integer platz) {
        // bookingService.scheduleBooking();
        return ResponseEntity.ok("ok");
    }
}
