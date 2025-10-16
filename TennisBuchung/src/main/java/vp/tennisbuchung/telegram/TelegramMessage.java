package vp.tennisbuchung.telegram;

import java.util.Objects;

public class TelegramMessage {

    public TelegramMessage(Long chatId, String message) {
	super();
	this.chatId = chatId;
	this.message = message;
    }

    private Long chatId;
    private String message;

    /**
     * @return the chatId
     */
    public Long getChatId() {
	return chatId;
    }

    /**
     * @param chatId the chatId to set
     */
    public void setChatId(Long chatId) {
	this.chatId = chatId;
    }

    /**
     * @return the message
     */
    public String getMessage() {
	return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
	this.message = message;
    }

    @Override
    public int hashCode() {
	return Objects.hash(chatId, message);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TelegramMessage other = (TelegramMessage) obj;
	return (chatId.longValue() == other.chatId.longValue()) && Objects.equals(message, other.message);
    }

}
