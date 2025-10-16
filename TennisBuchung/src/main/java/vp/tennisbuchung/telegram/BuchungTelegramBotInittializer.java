package vp.tennisbuchung.telegram;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BuchungTelegramBotInittializer {

    private final BuchungTelegramBot telegramBot;

    public BuchungTelegramBotInittializer(BuchungTelegramBot telegramBot) {
	this.telegramBot = telegramBot;
    }

    @EventListener({ ContextRefreshedEvent.class })
    public void init() {
	log.info("Initalize Telegram Bot");
	try {
	    TelegramBotsApi teleBotsApi = new TelegramBotsApi(DefaultBotSession.class);
	    teleBotsApi.registerBot(telegramBot);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
