package utils.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import models.Capabilities;
import utils.ObjectUtilities;
import utils.Printer;
import utils.StringUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static resources.Colors.*;

public class DriverFactory {

    static Printer log = new Printer(DriverFactory.class);

    public static AppiumDriver getDriver(String deviceName, Capabilities capabilities){
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

    public static DesiredCapabilities getConfig(Capabilities capabilities) {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        ObjectUtilities objUtils = new ObjectUtilities();

        Map<String, Object> capabilitiesMap = objUtils.getFields(MobileCapabilityType.class);
        Map<String, Object> androidCapabilities = objUtils.getFields(AndroidMobileCapabilityType.class);
        Map<String, Object> iosCapabilities = objUtils.getFields(IOSMobileCapabilityType.class);

        androidCapabilities.keySet().iterator().forEachRemaining(field ->
                capabilitiesMap.put(field, androidCapabilities.get(field))
        );

        iosCapabilities.keySet().iterator().forEachRemaining(field ->
                capabilitiesMap.put(field, iosCapabilities.get(field))
        );

        printObjectFields(capabilitiesMap);

        for (String key : capabilities.getConfig(capabilities).keySet()) {

            log.new Info("Setting "+ PURPLE + key + GRAY + " capability as: \"" + capabilities.getConfig(capabilities).get(key) + "\" " + RESET);



            log.new Important("FIELD: " + Capabilities.Capability.valueOf(key.toUpperCase()));
            log.new Important("VALUE: " + capabilities.getConfig(capabilities).get(key));

            desiredCapabilities.setCapability(
                    capabilitiesMap.get(Capabilities.Capability.valueOf(key.toUpperCase()).toString()).toString(),
                    capabilities.getConfig(capabilities).get(key)
            );
        }
        return desiredCapabilities;
    }

    public static void printObjectFields(Object object){
        List<Field> fields = List.of(object.getClass().getDeclaredFields());
        StringBuilder output = new StringBuilder();
        try {
            for (Field field:fields){
                String fieldName = new StringUtilities().firstLetterCapped(field.getName());
                output.append("\n").append(fieldName).append(" : ").append(field.get(object));
            }
            log.new Important("\nFields: " + output);
        }
        catch (IllegalAccessException e) {throw new RuntimeException(e);}
    }

    public void printModelGetterValues(Object object){
        Method[] methods = object.getClass().getDeclaredMethods();
        StringBuilder output = new StringBuilder();
        try {
            for (Method method:methods)
                if (method.getName().contains("get")){
                    String fieldName = new StringUtilities().firstLetterCapped(method.getName().replaceAll("get", ""));
                    output.append("\n").append(fieldName).append(" : ").append(method.invoke(object));
                }
            log.new Important("\nFields: " + output);
        }
        catch (InvocationTargetException | IllegalAccessException e) {throw new RuntimeException(e);}
    }
}
