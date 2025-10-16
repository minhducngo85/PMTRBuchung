package vp.tennisbuchung.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vp.tennisbuchung.dtos.Konto;
import vp.tennisbuchung.services.BookingService;

import vp.tennisbuchung.enums.Halle;
import vp.tennisbuchung.enums.Tage;
import vp.tennisbuchung.enums.Uhrzeit;

import static vp.tennisbuchung.enums.Dauer.ZWEI_STUNDE;
import static vp.tennisbuchung.enums.Tage.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("deprecation")
@Slf4j
@Component
@RequiredArgsConstructor
public class BuchungTelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final BookingService bookingService;

    private static List<TelegramMessage> alrts = new ArrayList<TelegramMessage>();

    private static List<Long> CHAT_IDs = new ArrayList<Long>();
    static {
	CHAT_IDs.add(-4882082400L); // Buchung PMTR
	CHAT_IDs.add(1685262085L); // My chat with bot

    }

    private boolean isValidChatId(Long chatId) {
	if (CHAT_IDs.contains(chatId)) {
	    return true;
	}
	return false;
    }

    @Override
    public void onUpdateReceived(Update update) {
	Konto konto1 = new Konto("Ngo", "Duisburg6789",1);
	Konto konto2 = new Konto("Pham", "Duisburg6789",2);
	Konto konto3 = new Konto("nguyen", "0000",3);
	if (update.hasMessage()) {
	    Message incomeMessage = update.getMessage();
	    Long chatId = incomeMessage.getChatId();
	    if (!isValidChatId(chatId)) {
		sendMessage(chatId, "Invalid chat id");
		return;
	    }
	    if (incomeMessage.hasText()) {
		String text = incomeMessage.getText();
		if (text.startsWith("/book")) {
		    String[] splited = text.split(" ");
		    if (splited.length < 5) {
			sendMessage(chatId, "Invalid command");
			return;
		    }
		    String kontoName = splited[1];
		    String heuteOrMorgen = splited[2];
		    String uhrZeitStr = splited[3];

		    sendMessage(chatId, "Scheduling booking now....");
		    Konto konto = konto1;
		    Tage tage = HEUTE;
		    Uhrzeit uhrzeit = Uhrzeit.getUhrZeit(uhrZeitStr);
		    int platzNumber = 1;
		    String platz = splited[4];
		    try {
			platzNumber = Integer.parseInt(platz);
		    } catch (NumberFormatException e) {
		    }

		    if (kontoName.equalsIgnoreCase("Pham")) {
			konto = konto2;
		    } else if (kontoName.equalsIgnoreCase("Nguyen")) {
			konto = konto3;
		    }
		    if (heuteOrMorgen.equalsIgnoreCase("Morgen")) {
			tage = Tage.MORGEN;
		    }

		    // Parsing Halle option
		    Halle halle = Halle.DUISBURG;
		    // Halle option
		    if (splited.length >= 6) {
			String halleString = splited[5];
			if (halleString.equalsIgnoreCase("M"))
			    halle = Halle.MUELHEIM;
		    }
		    String msg = "Konto: " + konto.lastname();
		    msg += "\nDatum: " + tage.getName();
		    msg += "\nUhrzeit: " + uhrzeit.getStringValue();
		    msg += "\nPlatz: " + platzNumber;
		    msg += "\nHalle: " + halle.getName();
		    log.info(msg);
		    sendMessage(chatId, msg);
		    msg = bookingService.scheduleBookingTrigger(tage, uhrzeit, halle, platzNumber, ZWEI_STUNDE, konto,
			    chatId);
		    sendMessage(chatId, msg);

		} else if (text.equalsIgnoreCase("/cancelBooking")) {
		    String msg = "To delete all pending booking jobs";
		    sendMessage(chatId, msg);

		    msg = bookingService.cancelBooking();
		    if (!msg.isBlank()) {
			sendMessage(chatId, msg);
		    }
		} else if (text.equalsIgnoreCase("/restart")) {
		    //String msg = "Application is being restarted! All pending jobs are now canceled";
		    //sendMessage(chatId, msg);
		    sendMessage(chatId, "Not supported command!");
		} else if (text.startsWith("/help")) {
		    String msg = "1. to book a tennis court\n/book [Konto] [Datum] [UhrZeit] [Platz] [Halle]\n\n";
		    msg += "Konto: Ngo|Pham|Nguyen\n";
		    msg += "Datum: Heute|Morgen\n";
		    msg += "Uhrzeit (HH:mm): 9:00|21:00|21:30\n";
		    msg += "Platz: 1|2|3\n";
		    msg += "Halle(optional): D|M\n";
		    msg += "D=Duisburg(default);M=Mülheim\n";
		    msg += "e.g. \n/book Ngo Heute 21:00 1\n";
		    msg += "/book Ngo Heute 21:00 1 M";
		    msg += "\n\n2. to cancel and deleted all pending bookings.\n /cancelBooking\n";
		    //msg += "\n\n3.to retsart application.\n /restart\n";

		    sendMessage(chatId, msg);
		}
	    }
	}
    }

    @Override
    public String getBotUsername() {
	return botName;
    }

    public String getBotToken() {
	return botToken;
    }

    public void sendMessage(Long chatId, String messageToSend) {
	SendMessage message = new SendMessage();
	message.setChatId(chatId);
	message.setText(messageToSend);
	try {
	    execute(message);
	} catch (TelegramApiException e) {
	    e.printStackTrace();
	}
    }

    @Scheduled(cron = "0/1 * * ? * *")
    public void sendMessageFromQueue() {
	if (!alrts.isEmpty()) {
	    Iterator<TelegramMessage> it = alrts.iterator();
	    alrts = new ArrayList<TelegramMessage>();
	    while (it.hasNext()) {
		TelegramMessage msg = it.next();
		sendMessage(msg.getChatId(), msg.getMessage());
	    }
	}
    }

    public static synchronized void addMessageToQueue(Long chatId, String messageToSend) {
	TelegramMessage message = new TelegramMessage(chatId, messageToSend);
	if (!alrts.contains(message)) {
	    alrts.add(message);
	}
    }

    @Override
    public void onRegister() {
	super.onRegister();
	log.info("On register!");
	for (Long chatId : CHAT_IDs) {
	    sendMessage(chatId, "Bot is started!");
	}
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {
	// TODO Auto-generated method stub
	super.clearWebhook();
	log.info("clearWebhook!");
    }

    @Override
    public void onClosing() {
	// TODO Auto-generated method stub
	super.onClosing();
	log.info("onClosing!");
    }

    @PreDestroy
    public void onDestroy() throws Exception {
	log.info("destroying!");
	for (Long chatId : CHAT_IDs) {
	    sendMessage(chatId, "Bot is shut down! All pending jobs are canceled");
	}
    }

}
