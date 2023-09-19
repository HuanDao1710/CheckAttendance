import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

    static final String CHROME_DRIVER_PATH = System.getProperty("user.dir") + File.separator + "drivers" + File.separator + "chromedriver";
    // window
    //static final String FIREFOX_DRIVER_PATH = System.getProperty("user.dir") + File.separator + "drivers" + File.separator + "geckodriver.exe";
    public static void main(String[] args) throws IOException,
        InterruptedException {

        System.setProperty("webdriver.gecko.driver", CHROME_DRIVER_PATH);

        ChromeOptions options = new ChromeOptions();

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        driver.get("https://hrm.falcongames.com/login");

        System.out.println(driver.getTitle());

        System.out.println("Entering username....");

        driver.findElement(By.id("tenDangNhap")).sendKeys(UserConstant.USERNAME);

        System.out.println("Entering password....");

        driver.findElement(By.id("matKhau")).sendKeys(UserConstant.PASSWORD);

        driver.findElement(By.id("loginButton")).click();

        driver.findElement(By.xpath("//span[text()=' Chấm Công']")).click();

        driver.findElement(By.xpath("//span[text()='Bảng Tổng hợp công']")).click();

        WebElement searchElement = driver.findElement(By.xpath("//input[contains(@placeholder, 'Tìm kiếm nhanh...')]"));
        searchElement.sendKeys(UserConstant.ID);

        Thread.sleep(6000);
        searchElement.sendKeys(Keys.ENTER);

        List<AttendanceRecord> listAttendanceRecord =
            driver.findElements(By.cssSelector("td.text-center.font-size"))
            .stream()
            .map(Application::getAttendanceRecord)
                .filter(Objects::nonNull)
                .toList();


        WebElement newestNewElement = driver.findElement(By.className("line-post-tiendo"));

        String newTitle = newestNewElement.findElement(By.className("clsTitleMXHNew")).getText();

        highlightElement(driver, newestNewElement);


        File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        // Get the dimensions of the <div> element
        int divX = newestNewElement.getLocation().getX();
        int divY = newestNewElement.getLocation().getY();
        int divWidth = newestNewElement.getSize().getWidth();
        int divHeight = newestNewElement.getSize().getHeight();

        String fileName = newTitle.replace(" ", "_").toLowerCase(Locale.ROOT) + ".png";

        // Crop the screenshot to include only the <div> element
        File croppedScreenshotFile = new File("/Users/liam/workspace/falcon-academy/selenium-101/output/" + fileName);
        FileUtils.copyFile(screenshotFile, croppedScreenshotFile);
        BufferedImage originalImage = ImageIO.read(screenshotFile);
        BufferedImage croppedImage = originalImage.getSubimage(divX, divY, divWidth, divHeight);
        ImageIO.write(croppedImage, "png", croppedScreenshotFile);

        try {
            Thread.sleep(10_000);
        } catch (Exception e) {
        }
        driver.quit();
    }

    static AttendanceRecord getAttendanceRecord(WebElement tdElement) {
        String date;
        String DATE_REGEX = "\\b\\d{2}/\\d{2}/\\d{4}\\b";
        String onClickAttribute = tdElement.findElement(By.cssSelector("span.time-quet-vao-ra")).getAttribute("onclick");
        Pattern pattern1 = Pattern.compile(DATE_REGEX);
        Matcher matcher1 = pattern1.matcher(onClickAttribute);
        if(matcher1.find()) {
            date = matcher1.group();
        } else {
            return null;
        }

        String TIME_REGEX = "^\\d{2}:\\d{2}";
        String text = tdElement.findElement(By.cssSelector("span.time-quet-vao-ra span")).getText().trim();
        Pattern pattern2 = Pattern.compile(TIME_REGEX);
        Matcher matcher2 = pattern2.matcher(text);
        if(!matcher2.matches()) {
            return null;
        }

        String[] times =text.split("\n");
        if(times.length == 1) {
            return new AttendanceRecord(date, times[0], "");
        }
        return new AttendanceRecord(date, times[0], times[1]);
    }

    static void quitDriverWhenShutdown(WebDriver driver) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (driver != null) {
                driver.quit();
            }
        }));
    }

    public static void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid yellow'", element);
    }
}
