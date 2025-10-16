# Tennis Buchung App using Telegram Bot
## Getting Started
- Clone the project from Git:
    git clone https://github.com/vietphe/TennisBuchung.git
- Import the project as a Maven project into IntelliJ IDEA or Eclipse.
- Open the BookingStartupRunner class.
- Adjust the parameters as needed (e.g., date, time, credentials, etc.).
- Run the application as spriing boot app
- Telegram bot is started
- add your bot to chat and type /help.


## Telegram Bot
- search for BotFather and create a new bot
- create a application.properties (template: application_template.properties)
- update bot.name and bot.token in application.properties
- BuchungTelegramBot.java
 /** Ou have to update the list of allowed chat ids*/
 private static List<Long> CHAT_IDs = new ArrayList<Long>();