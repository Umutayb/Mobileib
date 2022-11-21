package utils.appium;

import io.appium.java_client.AppiumDriver;
import org.json.simple.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.FileUtilities;
import utils.Printer;
import java.net.URL;
import java.time.Duration;

import static resources.Colors.*;
import static utils.FileUtilities.properties;
import static utils.appium.ServiceFactory.service;

public class DriverFactory {

    static Printer log = new Printer(DriverFactory.class);
    static FileUtilities.Json jsonUtils = new FileUtilities.Json();

    public static AppiumDriver getDriver(String deviceName, JSONObject capabilities){
        DesiredCapabilities desiredCapabilities = getConfig(capabilities);
        try {
            URL url;
            if (service == null) {
                String address = properties.getProperty("address", "0.0.0.0");
                String port = properties.getProperty("port", "4723");
                url = new URL("http://" + address + ":" + port + "/wd/hub");
            }
            else url = service.getUrl();

            AppiumDriver driver = new AppiumDriver(url, desiredCapabilities);
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

    public static DesiredCapabilities getConfig(JSONObject capabilities) {
        log.new Info("Setting capabilities...");
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        for (Object key : capabilities.keySet()) desiredCapabilities.setCapability((String) key, capabilities.get(key));
        return desiredCapabilities;
    }
}
