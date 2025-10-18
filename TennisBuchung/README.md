# Tennis Buchung App using Telegram Bot
##Pupose
- to automate the last minute booking of tennis court of PMRT center in Duisburg and Mülheim (NRW, Germany) with a monthly ticket
- the project is  developed in SringBoot framework wiht Selenium Webdriver and Telegram Bot

### Booking flow:
1. User triggers a booking from a telegram group chat
2. Telegram bot listens the command
3. Based on the received command, telegram bot will start the web browser and simulate the user interactions in web ui 

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
 /** You have to update the list of allowed chat ids*/
 private static List<Long> CHAT_IDs = new ArrayList<Long>();
 
## Author
- Viet Phe (@vietphe)
- Minh Duc Ngo (@minhducngo85)