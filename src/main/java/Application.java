import org.openqa.selenium.*;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

    static final String CHROME_DRIVER_PATH = System.getProperty("user.dir") + File.separator + "drivers" + File.separator + "chromedriver";
    public static void main(String[] args) throws InterruptedException {

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

        driver.get("https://hrm.falcongames.com/BangCongDong/Index?tab=%23bangchamcong");

        Thread.sleep(5000);
        WebElement searchElement = driver.findElement(By.xpath("//input[contains(@placeholder, 'Tìm kiếm nhanh...')]"));
        searchElement.sendKeys(UserConstant.ID);
        searchElement.sendKeys(Keys.ENTER);

        System.out.println("Getting data...");
        List<AttendanceRecord> listAttendanceRecord =
            driver.findElements(By.cssSelector("td.text-center.font-size"))
            .stream()
            .map(Application::getAttendanceRecord)
                .filter(Objects::nonNull)
                .toList();
        List<AttendanceRecord> listLate = listAttendanceRecord.stream()
            .filter(AttendanceRecord::isBeingLateLeaveEarly)
            .toList();
        List<AttendanceRecord> listMissAttendance = listAttendanceRecord.stream()
            .filter(AttendanceRecord::hasMissedAttendance)
            .toList();

        if(CollectionUtils.isNotEmpty(listLate) || CollectionUtils.isNotEmpty(listMissAttendance)) {
            System.out.println("Sending Email...");
            EmailService.sendEmail(listLate, listMissAttendance);
        }

        if(CollectionUtils.isNotEmpty(listMissAttendance)) {
            //Tự động bổ sung công
            for(AttendanceRecord obj : listMissAttendance) {
                Thread.sleep(1000);
                driver.get("https://hrm.falcongames.com/BoSungCong/Create");
                var inputElement = driver.findElement(By.id("txtNgayLamViec"));
                String script = "arguments[0].removeAttribute('disabled'); arguments[0].value = '"+obj.getDate()+"';";
                ((JavascriptExecutor) driver).executeScript(script, inputElement);
                new Select(driver.findElement(By.id("lyDoDieuChinh"))).selectByValue("BSC_QuenChamCong");
                //driver.findElement(By.className("btnXuLyPhieuNew")).click();
                System.out.println("đã bổ sung công cho ngày: " + obj.getDate());
            }
        }
        driver.quit();
    }

    static AttendanceRecord getAttendanceRecord(WebElement tdElement) {
        String date;
        String dateRegex = "\\b\\d{2}/\\d{2}/\\d{4}\\b";
        String onClickAttribute = tdElement.findElement(By.cssSelector("span.time-quet-vao-ra")).getAttribute("onclick");
        Pattern pattern1 = Pattern.compile(dateRegex);
        Matcher matcher1 = pattern1.matcher(onClickAttribute);
        if(matcher1.find()) {
            date = matcher1.group();
        } else {
            return null;
        }
        String timeRegex = "^\\d{2}:\\d{2}.*";
        String text = tdElement.findElement(By.cssSelector("span.time-quet-vao-ra span")).getText().trim();
        Pattern pattern2 = Pattern.compile(timeRegex);
        Matcher matcher2 = pattern2.matcher(text);
        if(!matcher2.find()) {
            return null;
        }
        String[] times =text.split("\n");
        if(times.length == 1) {
            return new AttendanceRecord(date, times[0], "");
        }
        return new AttendanceRecord(date, times[0], times[1]);
    }
}
