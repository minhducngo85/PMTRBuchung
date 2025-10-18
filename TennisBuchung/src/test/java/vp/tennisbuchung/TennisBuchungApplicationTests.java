package vp.tennisbuchung;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import vp.tennisbuchung.startup.BookingStartupRunner;

@Slf4j
@SpringBootTest
class TennisBuchungApplicationTests {

    @Test
    void contextLoads() {
    }
    
    @Test
    void test() {
	log.info("Test");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	LocalDateTime now = LocalDateTime.now();
	String formattedDateTime = now.format(formatter); // "1986-04-08 12:30"
	log.info("LocaldateTime: " + formattedDateTime);

    }

}
