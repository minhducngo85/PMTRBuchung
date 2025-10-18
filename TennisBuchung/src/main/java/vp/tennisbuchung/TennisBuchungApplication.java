package vp.tennisbuchung;

import java.util.TimeZone;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TennisBuchungApplication {

    private static ConfigurableApplicationContext context;
    
    public static void main(String[] args) {
	TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        context = SpringApplication.run(TennisBuchungApplication.class, args);
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(ConfigurableApplicationContext.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }
}
