package vp.tennisbuchung.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfig {
    @Bean
    public ScheduledExecutorService bookingScheduler() {
        return Executors.newScheduledThreadPool(2);
    }
}
