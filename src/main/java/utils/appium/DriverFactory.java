package utils.appium;

import com.google.gson.JsonObject;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.FileUtilities;
import utils.Printer;
import java.time.Duration;

import static resources.Colors.*;

public class DriverFactory {

    static Printer log = new Printer(DriverFactory.class);
    static FileUtilities.Json jsonUtils = new FileUtilities.Json();


    public static AppiumDriver getDriver(String deviceName, JsonObject capabilities){
        DesiredCapabilities desiredCapabilities = getConfig(capabilities);
        try {
            AppiumDriver driver = new AppiumDriver(ServiceFactory.service.getUrl(), desiredCapabilities);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            log.new Important(deviceName+GRAY+" was selected");
            return driver;
        }
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.new Info("Please make sure "+PURPLE+"Appium "+GRAY+"is on & verify the port that its running on at 'resources/test.properties'."+RESET);
                throw new RuntimeException(YELLOW+gamma+RESET);
            }
            else throw new RuntimeException(YELLOW+"Something went wrong while selecting a driver "+"\n\t"+RED+gamma+RESET);
        }
    }

    public static DesiredCapabilities getConfig(JsonObject capabilities) {
        log.new Info("Setting capabilities...");
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        for (String key : capabilities.keySet()) desiredCapabilities.setCapability(key, capabilities.get(key));
        return desiredCapabilities;
    }
}
