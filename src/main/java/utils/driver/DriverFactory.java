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
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static resources.Colors.*;

public class DriverFactory {

    static Printer log = new Printer(DriverFactory.class);

    public static AppiumDriver<MobileElement> getDriver(String deviceName, AppiumDriver<MobileElement> driver, Capabilities capabilities){
        DesiredCapabilities desiredCapabilities = getConfig(capabilities);
        try {
            switch (capabilities.platformName.toLowerCase()){
                case "android":
                    driver = new AndroidDriver<>(new URL("http:/127.0.0.1:4723/wd/hub"), desiredCapabilities);
                    break;
                case "ios":
                    driver = new IOSDriver<>(new URL("http:/127.0.0.1:4723/wd/hub"), desiredCapabilities);
                    break;
            }
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            log.new Important(deviceName+GRAY+" was selected");
            return driver;
        }
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.new Info("Please make sure the "+PURPLE+"Selenium Grid "+GRAY+"is on & verify the port that its running on at 'resources/test.properties'."+RESET);
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
        Properties properties = new Properties();
        try {properties.load(new FileReader("src/test/resources/test.properties"));}
        catch (IOException e) {e.printStackTrace();}

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        for (String key : capabilities.getConfig(capabilities).keySet()) {

            log.new Info("Setting "+PURPLE + key + GRAY + " capability as: \"" + capabilities.getConfig(capabilities).get(key) + "\" " + RESET);

            switch (key){
                case "deviceName":
                    desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "avdName":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.AVD, capabilities.getConfig(capabilities).get(key));
                    break;

                case "automationName":
                    desiredCapabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "platformVersion":
                    desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, capabilities.getConfig(capabilities).get(key));
                    break;

                case "platformName":
                    desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "language":
                    desiredCapabilities.setCapability(MobileCapabilityType.LANGUAGE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "app":
                    desiredCapabilities.setCapability(MobileCapabilityType.APP, capabilities.getConfig(capabilities).get(key));
                    break;

                case "applicationName":
                    desiredCapabilities.setCapability(MobileCapabilityType.APPLICATION_NAME, capabilities.getConfig(capabilities).get(key));
                    break;

                case "locale":
                    desiredCapabilities.setCapability(MobileCapabilityType.LOCALE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "reset":
                    desiredCapabilities.setCapability(MobileCapabilityType.FULL_RESET, capabilities.getConfig(capabilities).get(key));
                    break;

                case "noReset":
                    desiredCapabilities.setCapability(MobileCapabilityType.NO_RESET, capabilities.getConfig(capabilities).get(key));
                    break;

                case "orientation":
                    desiredCapabilities.setCapability(MobileCapabilityType.ORIENTATION, capabilities.getConfig(capabilities).get(key));
                    break;

                case "rotatable":
                    desiredCapabilities.setCapability(MobileCapabilityType.ROTATABLE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "takesScreenshot":
                    desiredCapabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, capabilities.getConfig(capabilities).get(key));
                    break;

                case "UDID":
                    desiredCapabilities.setCapability(MobileCapabilityType.UDID, capabilities.getConfig(capabilities).get(key));
                    break;

                case "unexpectedAlertBehaviour":
                    desiredCapabilities.setCapability(MobileCapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, capabilities.getConfig(capabilities).get(key));
                    break;

                case "appPackage":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, capabilities.getConfig(capabilities).get(key));
                    break;

                case "appActivity":
                    desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, capabilities.getConfig(capabilities).get(key));
                    break;

                default:
                    Assert.fail(GRAY+ "The capability type '" +key+ "' was undefined."+RED+"\nTest Failed."+RESET);
            }

        }

        return desiredCapabilities;

    }
}
