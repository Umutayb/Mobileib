package utils.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.Assert;
import org.openqa.selenium.remote.DesiredCapabilities;
import resources.Capabilities;
import utils.Printer;
import utils.StringUtilities;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import static resources.Colors.*;

public class DriverFactory {

    static Printer log = new Printer(DriverFactory.class);
    static Properties properties = new Properties();

    public static AppiumDriver<MobileElement> getDriver(String driverName, AppiumDriver<MobileElement> driver, Capabilities capabilities){
        StringUtilities strUtils = new StringUtilities();
        DesiredCapabilities desiredCapabilities = getConfig(capabilities);
        try {
            properties.load(new FileReader("src/test/resources/test.properties"));

            if (driverName == null)
                driverName = strUtils.firstLetterCapped(properties.getProperty("device"));

            switch (driverName.toLowerCase()){
                case "android":
                    driver = new AndroidDriver<>(new URL("http:/127.0.0.1:4723/wd/hub"), desiredCapabilities);
                    break;

                case "ios":
                    driver = new IOSDriver<>(new URL("http:/127.0.0.1:4723/wd/hub"), desiredCapabilities);
                    break;
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            driver.manage().window().maximize();
            log.new important(driverName+GRAY+" was selected");

            return driver;

        }
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.new info("Please make sure the "+PURPLE+"Selenium Grid "+GRAY+"is on & verify the port that its running on at 'resources/test.properties'."+RESET);
                Assert.fail(YELLOW+gamma+RESET);
            }
            else {
                Assert.fail(YELLOW+"Something went wrong while selecting a driver "+"\n\t"+RED+gamma+RESET);
            }
            driver.quit();
            return null;
        }
    }

    public static DesiredCapabilities getConfig(Capabilities capabilities) {

        try {properties.load(new FileReader("src/test/resources/test.properties"));}
        catch (IOException e) {e.printStackTrace();}

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        for (String key : capabilities.getConfig(capabilities).keySet()) {

            log.new info("Setting " + key + " capability as: \"" + capabilities.getConfig(capabilities).get(key) + "\" " + RESET);

            switch (key.toLowerCase()){
                case "device name":
                    desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "avd name":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.AVD, capabilities.getConfig(capabilities).get(key));
                    break;

                case "automation name":
                    desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "platform version":
                    desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, capabilities.getConfig(capabilities).get(key));
                    break;

                case "platform name":
                    desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "language":
                    desiredCapabilities.setCapability(MobileCapabilityType.LANGUAGE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "app":
                    desiredCapabilities.setCapability(MobileCapabilityType.APP, capabilities.getConfig(capabilities).get(key));
                    break;

                case "application name":
                    desiredCapabilities.setCapability(MobileCapabilityType.APPLICATION_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "locale":
                    desiredCapabilities.setCapability(MobileCapabilityType.LOCALE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "":
                    desiredCapabilities.setCapability(MobileCapabilityType.FULL_RESET, capabilities.getConfig(capabilities).get(key));
                    break;

                case "no reset":
                    desiredCapabilities.setCapability(MobileCapabilityType.NO_RESET, capabilities.getConfig(capabilities).get(key));
                    break;

                case "orientation":
                    desiredCapabilities.setCapability(MobileCapabilityType.ORIENTATION, capabilities.getConfig(capabilities).get(key));
                    break;

                case "rotatable":
                    desiredCapabilities.setCapability(MobileCapabilityType.ROTATABLE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "takes screenshot":
                    desiredCapabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, capabilities.getConfig(capabilities).get(key));
                    break;

                case "udid":
                    desiredCapabilities.setCapability(MobileCapabilityType.UDID, capabilities.getConfig(capabilities).get(key));
                    break;

                case "unexpected alert behaviour":
                    desiredCapabilities.setCapability(MobileCapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, capabilities.getConfig(capabilities).get(key));
                    break;

                case "app package":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "app activity":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, capabilities.getConfig(capabilities).get(key));
                    break;

                default:
                    Assert.fail(GRAY+ "The capability type '" +key+ "' was undefined."+RED+"\nTest Failed."+RESET);
            }

        }

        return desiredCapabilities;

    }
}
