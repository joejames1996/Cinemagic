package org.softwire.training.cinemagic.integration;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.google.common.base.Function;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.softwire.training.cinemagic.models.Cinema;
import org.softwire.training.cinemagic.models.Film;
import org.softwire.training.cinemagic.models.Screen;
import org.softwire.training.cinemagic.models.Showing;
import org.softwire.training.cinemagic.services.CinemaService;
import org.softwire.training.cinemagic.services.FilmService;
import org.softwire.training.cinemagic.services.ScreenService;
import org.softwire.training.cinemagic.services.ShowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class BookingTest {
    @Rule
    public final WebDriverLoggingRule webDriverLoggingRule = new WebDriverLoggingRule();

    @LocalServerPort
    protected Integer port;

    @Autowired
    protected WebDriver driver;

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private ShowingService showingService;

    @Autowired
    private ScreenService screenService;

    private static Screen testScreen;
    private static Film testFilm;

    @Test
    public void testTitle() {
        navigateToBookingPage();
        assertThat("Cinemagic", equalTo(driver.getTitle()));
    }

    @Test
    public void testMainlineBookingFlow() {
        String cinemaName = "Test Cinema";
        String screenName = "Test Screen";
        createTestCinemaAndScreen(cinemaName, screenName);

        String filmName = "Test Film";
        createTestFilm(filmName);

        createTestShowing();

        navigateToBookingPage();
        waitForElement(By.className("cinema-select"));

        // Select the cinema
        WebElement cinemaWidget = driver.findElement(By.className("cinema-select"));
        Select select = new Select(cinemaWidget.findElement(By.tagName("select")));
        select.selectByVisibleText(cinemaName);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        waitForElement(By.className("showing-select"));
        driver.findElement(By.className("showing-selection-button")).click();

        waitForElement(By.className("seat-select"));
        driver.findElement(By.className("free")).click();
        driver.findElement(By.className("submit-button")).click();

        waitForElement(By.className("booking-success"));
    }

    private void createTestCinemaAndScreen(String cinemaName, String screenName) {
        Cinema cinema = new Cinema();
        cinema.setName(cinemaName);

        cinemaService.create(cinema);

        Screen screen = new Screen();
        screen.setCinema(cinema);
        screen.setName(screenName);
        screen.setRows(5);
        screen.setRowWidth(5);

        screenService.create(screen);

        List<Screen> screens = new ArrayList<Screen>()
        {{
            add(screen);
        }};
        cinema.setScreens(screens);

        testScreen = screen;
    }

    private void createTestFilm(String filmName)
    {
        Film film = new Film();
        film.setName(filmName);
        film.setLengthMinutes(60);

        testFilm = film;
        filmService.create(film);
    }

    private void createTestShowing()
    {
        Showing showing = new Showing();
        showing.setFilm(testFilm);
        showing.setScreen(testScreen);
        showing.setTime(Instant.now().plus(1, ChronoUnit.DAYS));

        showingService.create(showing);
    }

    private void navigateToBookingPage() {
        driver.get("http://localhost:" + port + "/booking");
    }

    @SuppressWarnings("ConstantConditions")
    private WebElement waitForElement(By locator) {
        return shortWait().until((Function<? super WebDriver, WebElement>) webDriver -> webDriver.findElement(locator));
    }

    private WebDriverWait shortWait() {
        return new WebDriverWait(driver, 10, 100);
    }
}
