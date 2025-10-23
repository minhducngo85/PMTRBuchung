package vp.tennisbuchung.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import vp.tennisbuchung.dtos.Konto;
import vp.tennisbuchung.enums.Dauer;
import vp.tennisbuchung.enums.Halle;
import vp.tennisbuchung.enums.Tage;
import vp.tennisbuchung.enums.Uhrzeit;
import vp.tennisbuchung.telegram.BuchungTelegramBot;
import vp.tennisbuchung.telegram.TelegramMessage;

import java.io.File;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static vp.tennisbuchung.enums.Halle.DUISBURG;
import static vp.tennisbuchung.enums.Halle.MUELHEIM;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final ScheduledExecutorService bookingScheduler;

    private List<ScheduledFuture<?>> futures = new ArrayList<ScheduledFuture<?>>();

    /**
     * Mở 2 Chrome riêng biệt: - Luồng Early click sớm 0,5s - Luồng Exact click đúng
     * giờ
     */
    public String scheduleBookingTrigger(Tage tage, Uhrzeit bookingTime, Halle halle, int platz, Dauer dauer,
	    Konto konto, Long chatId) {
	log.warn("scheduleBookingTrigger ...........");
	LocalDateTime now = LocalDateTime.now();
	LocalDate bookingDate = tage.getBookingDate();
	LocalDateTime bookingDateTime = bookingDate.atTime(bookingTime.getHour(), bookingTime.getMinute());
	// Preaparation time for each account should be different. Otherwise it makes
	// chrome driver broken if multiple user oen web browser at the same time
	LocalDateTime triggerTime = bookingDateTime.minusDays(1).minusMinutes(konto.preparationBeforeMinute()); // begin
														// before
														// 1-3
														// min.

	if (triggerTime.isAfter(bookingDateTime)) {
	    log.warn("Too late to begin booking");
	    return "";
	}

	long delay = Duration.between(now, triggerTime).toMillis();
	Runnable earlyTask = () -> {
	    Thread.currentThread().setName("BookingThread-Early");
	    openChromeAndBook(tage, bookingTime, halle, platz, dauer, konto, chatId);
	};

	Runnable exactTask = () -> {
	    Thread.currentThread().setName("BookingThread-Exact");
	    openChromeAndBook(tage, bookingTime, halle, platz, dauer, konto, chatId);
	};
	String ret = "";
	if (delay <= 0) {
	    new Thread(earlyTask).start();
	    new Thread(exactTask).start();
	    ret = konto.lastname() + ": Booking is running...!";
	} else {
	    ScheduledFuture<?> earlyFuture = bookingScheduler.schedule(() -> new Thread(earlyTask).start(), delay,
		    TimeUnit.MILLISECONDS);
	    ScheduledFuture<?> exactFuture = bookingScheduler.schedule(() -> new Thread(exactTask).start(), delay,
		    TimeUnit.MILLISECONDS);
	    futures.add(earlyFuture);
	    futures.add(exactFuture);
	    ret = konto.lastname() + ": Booking will be started in " + convertSecondsToString(delay / 1000);
	}
	return ret;
    }

    // to cancel all pding bokking jobs
    public String cancelBooking() {
	for (ScheduledFuture<?> future : futures) {
	    log.info("Before Cancel - Task is done : " + future.isDone());
	    log.info("Before Cancel - Task is cancel : " + future.isCancelled());
	    if (!future.isDone()) {
		future.cancel(false);
	    }
	    log.info("After Cancel - Task is done : " + future.isDone());
	    log.info("After Cancel - Task is cancel : " + future.isCancelled());
	}
	futures = new ArrayList<ScheduledFuture<?>>();
	return "All scheduled bookings are now canceled";
    }

    private String convertSecondsToString(long totalSecs) {
	long hours = totalSecs / 3600;
	long minutes = (totalSecs % 3600) / 60;
	long seconds = totalSecs % 60;

	String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	return timeString;
    }

    private WebDriver initializeWebDriver(int windowLength) {
	ChromeOptions options = new ChromeOptions();
	options.addArguments("--start-maximized");
	options.addArguments("--disable-notifications");
	options.addArguments("--disable-infobars");
	options.addArguments("--no-sandbox");
	// Nếu muốn chạy headless, bỏ comment
	if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
	    // Head less for Linux
	    options.addArguments("--headless=new");
	}
	// tạo profile riêng cho mỗi luồng
	options.addArguments("--user-data-dir=/tmp/chrome-profile-" + Thread.currentThread().getName() + "-"
		+ System.currentTimeMillis());

	WebDriver driver = new ChromeDriver(options);
	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	driver.manage().window().setSize(new Dimension(550, windowLength));
	return driver;
    }

    // Get booking status of a given halle
    public void openChromeAndGetStatus(Tage tage, Halle halle, Konto konto, Long chatId) {
	BuchungTelegramBot.addMessageToQueue(chatId, konto.lastname() + ": Opening webbrowser ...");

	WebDriver driver = initializeWebDriver(1100);
	try {
	    driver.get("https://app.tennis04.com/de/pmtr/login?returnUrl=%2Fde%2Fpmtr%2Fbuchungsplan");

	    login(driver, konto);
	    Thread.sleep(2000);

	    selectHalle(driver, halle);
	    Thread.sleep(1000);

	    selectDay(driver, tage);
	    new Actions(driver).scrollByAmount(0, 100).perform();
	    Thread.sleep(1000);

	    String fileName = "Screenshot_" + System.currentTimeMillis() + ".png";
	    String folder = System.getProperty("user.home");
	    fileName = folder + "/" + fileName;
	    File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	    FileUtils.copyFile(src, new File(fileName));

	    TelegramMessage message = new TelegramMessage(chatId, "image:" + fileName);
	    message.setAdditionalInfo(halle.getName().split("_")[0] + " " + tage.getName());
	    BuchungTelegramBot.addMessageToQueue(chatId, message);
	    Thread.sleep(10000);

	} catch (Exception e) {
	    log.error("An error occurs while fetching status", e);
	} finally {
	    log.info("Fetching status done!");
	    driver.quit();
	}
    }

    /**
     * Mở Chrome riêng và book
     */
    private void openChromeAndBook(Tage tage, Uhrzeit uhrzeit, Halle halle, int platz, Dauer dauer, Konto konto,
	    Long chatId) {
	BuchungTelegramBot.addMessageToQueue(chatId, konto.lastname() + ": Opening webbrowser ...");

	WebDriver driver = initializeWebDriver(900);
	try {
	    driver.get("https://app.tennis04.com/de/pmtr/login?returnUrl=%2Fde%2Fpmtr%2Fbuchungsplan");

	    login(driver, konto);
	    Thread.sleep(2000);

	    selectHalle(driver, halle);
	    Thread.sleep(1000);

	    selectDay(driver, tage);
	    Thread.sleep(1000);

	    openBookingModal(driver);
	    Thread.sleep(2000);

	    processBuchung(driver, tage, uhrzeit, platz, dauer, chatId, konto.lastname());

	    Thread.sleep(10000);

	} catch (Exception e) {
	    log.error("An error occurs while processing tennis buchen", e);
	} finally {
	    log.info("Buchen prepared successfully. Please check all opened windows to see results");

	}

    }

    private void login(WebDriver driver, Konto konto) {
	WebElement nameInput = driver.findElement(By.cssSelector("dx-text-box[formcontrolname='name'] input"));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nameInput);
	nameInput.sendKeys(konto.lastname());

	WebElement passwordInput = driver.findElement(By.xpath("//dx-text-box[@formcontrolname='password']//input"));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", passwordInput);
	passwordInput.sendKeys(konto.password());

	WebElement loginButton = driver.findElement(By.xpath("//dx-button[@text='anmelden']"));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
	loginButton.click();
    }

    private void selectHalle(WebDriver driver, Halle halle) {
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
	WebElement halleTab = null;
	if (halle == DUISBURG) {
	    halleTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
		    "//div[@class='dx-item dx-tab' and .//dxi-item[normalize-space(text())='Duisburg_Tennishalle']]")));
	    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", halleTab);
	    assert halleTab != null;
	    halleTab.click();
	} else if (halle == MUELHEIM) {
	    // default = Muelheim -> nothign to do
	    // halleTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
	    // "//div[@class='dx-item dx-tab' and
	    // .//dxi-item[normalize-space(text())='Muelheim_Tennishalle']]")));
	}

    }

    public void selectDay(WebDriver driver, Tage tage) {
	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
	WebElement nextDayButton = wait
		.until(ExpectedConditions.elementToBeClickable(By.xpath("//dx-button[@icon='chevronnext']")));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextDayButton);
	for (int i = 0; i < tage.getNumOfNextClicks(); i++) {
	    nextDayButton.click();
	}
    }

    private void openBookingModal(WebDriver driver) {
	// Try to click on element until this is clickebale
	List<WebElement> eventElements = driver
		.findElements(By.xpath("//div[@class='fc-bgevent' and @id='eundefined']"));
	for (WebElement element : eventElements) {
	    try {
		element.click();
		return;
	    } catch (Exception e) {
		log.info("Try to click on the next item");
	    }
	}
    }

    private void processBuchung(WebDriver driver, Tage tage, Uhrzeit uhrzeit, Integer platz, Dauer dauer, Long chatId,
	    String kontoName) throws InterruptedException {
	WebElement button = driver.findElement(By.xpath("//dx-button[@name='fromNext' and @icon='chevronnext']"));
	WebElement from = driver.findElement(By.xpath("//input[@type='hidden' and @name='from']"));

	for (int i = 0; i < uhrzeit.getNumOfNextClicks(); i++) {
	    button.click();
	    Thread.sleep(500);
	    // check if the value is equal to expected value
	    String fromValue = from.getAttribute("value");
	    if (fromValue.equalsIgnoreCase(uhrzeit.getStringValue())) {
		break;
	    }
	}

	WebElement button2 = driver.findElement(By.xpath("//dx-button[@name='durationNext' and @icon='chevronnext']"));
	for (int i = 0; i < dauer.getValue(); i++) {
	    button2.click();
	    Thread.sleep(500);
	}

	WebElement dropdownButton = driver.findElement(By.className("dx-list-group-header-indicator"));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", dropdownButton);
	dropdownButton.click();

	List<WebElement> elements = driver
		.findElements(By.xpath("//div[@class='dx-template-wrapper dx-item-content dx-list-item-content']"));
	Thread.sleep(1000);
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", elements.get(platz - 1));
	elements.get(platz - 1).click();

	WebElement buchen = driver.findElement(By.xpath("//dx-button//span[text()='Buchen']"));
	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buchen);

	long offsetMillis = Thread.currentThread().getName().contains("Early") ? 100 : 0; // bam dat truoc nua giay
	if (tage.equals(Tage.HEUTE)) {
	    scheduleClick(buchen, uhrzeit, offsetMillis, chatId, driver, true, kontoName);
	} else {
	    scheduleClick(buchen, uhrzeit, offsetMillis, chatId, driver, false, kontoName);
	}
    }

    private void scheduleClick(WebElement buchen, Uhrzeit uhrzeit, long offsetMillis, long chatId, WebDriver driver,
	    boolean isHeute, String kontoName) {
	long delay = 0;

	if (!isHeute) {
	    LocalTime now = LocalTime.now();
	    LocalTime targetTime = LocalTime.of(uhrzeit.getHour(), uhrzeit.getMinute(), 0);

	    delay = now.until(targetTime, ChronoUnit.MILLIS) - offsetMillis;
	    log.info("scheduleClick ... delay = " + delay / 1000 + " seconds");
	    if (delay <= 0) {
		delay = 0;
	    }
	}

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	scheduler.schedule(() -> {
	    String alertMsg = "";
	    if (Strings.isNotEmpty(kontoName)) {
		alertMsg = kontoName + ": ";
	    }
	    try {
		buchen.click();
		if (Strings.isNotEmpty(kontoName)) {
		    String clickInfo = kontoName + ": 'Buchen' clicked! Evaluating the result...";
		    log.info(clickInfo);
		    BuchungTelegramBot.addMessageToQueue(chatId, clickInfo);
		}

		// Wait until alert text gets updated
		try {
		    Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}

		// Collecting all alert messages and add them to the telegram message queue.
		// Notifcation caption
		List<String> alertElementGroup1 = new ArrayList<String>();
		alertElementGroup1.add(
			"/html/body/div[4]/div[2]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/p");
		alertElementGroup1.add(
			"/html/body/div[4]/div[3]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/p");
		alertElementGroup1.add(
			"/html/body/div[4]/div[4]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/p");

		// Notification description
		List<String> alertElementGroup2 = new ArrayList<String>();
		alertElementGroup2.add(
			"/html/body/div[4]/div[2]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/ul/li");
		alertElementGroup2.add(
			"/html/body/div[4]/div[3]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/ul/li");
		alertElementGroup2.add(
			"/html/body/div[4]/div[4]/div/t04-modal-wrapper/t04-create-booking/div[1]/t04-alertmessage/div/ul/li");

		// Buchung erfolgreich erstellt
		List<String> successfulElementGroup = new ArrayList<String>();
		// successfulElementGroup.add("/html/body/t04-modal/div[1]/div/t04-modalmessage/div/p");
		successfulElementGroup.add("/html/body/t04-modal/div[1]/div/t04-modalmessage/div/ul/li");
		// go over alert group 1
		for (String alertXPath : alertElementGroup1) {
		    try {
			WebElement pElement = driver.findElement(By.xpath(alertXPath));
			alertMsg += pElement.getText() + "\n";
			break;
		    } catch (NoSuchElementException e) {
			log.error("Element not found: " + alertXPath);
		    }
		}
		// go over alert group 2
		for (String alertXPath : alertElementGroup2) {
		    try {
			WebElement pElement = driver.findElement(By.xpath(alertXPath));
			alertMsg += pElement.getText() + "\n";
			break;
		    } catch (NoSuchElementException e) {
			log.error("Element not found: " + alertXPath);
		    }
		}

		// go over successfulElementGroup
		for (String alertXPath : successfulElementGroup) {
		    try {
			WebElement pElement = driver.findElement(By.xpath(alertXPath));
			alertMsg += pElement.getText() + "\n";
		    } catch (NoSuchElementException e) {
			log.error("Element not found: " + alertXPath);
		    }
		}

		if (!alertMsg.isEmpty()) {
		    log.info(alertMsg);
		    BuchungTelegramBot.addMessageToQueue(chatId, alertMsg);
		}
	    } catch (Exception e) {
		log.error("Failed to click 'Buchen'", e);
		alertMsg += kontoName + ": Failed to click 'Buchen'";
		BuchungTelegramBot.addMessageToQueue(chatId, alertMsg);
	    }
	    // close web driver
	    try {
		Thread.sleep(20 * 1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    driver.quit();
	}, delay, TimeUnit.MILLISECONDS);
    }
}
