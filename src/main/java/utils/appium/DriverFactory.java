package utils.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import models.Capabilities;
import utils.ObjectUtilities;
import utils.Printer;
import utils.StringUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
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

        Map<String, String> capabilitiesMap = new HashMap<>();

        Arrays.stream(CapabilityType.class.getDeclaredFields()).iterator().forEachRemaining(field ->
                {
                    field.setAccessible(true);
                    try {capabilitiesMap.put(field.getName(), field.get(CapabilityType.class).toString());}
                    catch (IllegalAccessException e) {throw new RuntimeException(e);}
                }
        );

        Arrays.stream(MobileCapabilityType.class.getDeclaredFields()).iterator().forEachRemaining(field ->
                {
                    field.setAccessible(true);
                    try {capabilitiesMap.put(field.getName(), field.get(MobileCapabilityType.class).toString());}
                    catch (IllegalAccessException e) {throw new RuntimeException(e);}
                }
        );

        Arrays.stream(AndroidMobileCapabilityType.class.getDeclaredFields()).iterator().forEachRemaining(field ->
                {
                    field.setAccessible(true);
                    try {capabilitiesMap.put(field.getName(), field.get(AndroidMobileCapabilityType.class).toString());}
                    catch (IllegalAccessException e) {throw new RuntimeException(e);}
                }
        );

        Arrays.stream(IOSMobileCapabilityType.class.getDeclaredFields()).iterator().forEachRemaining(field ->
                {
                    field.setAccessible(true);
                    try {capabilitiesMap.put(field.getName(), field.get(IOSMobileCapabilityType.class).toString());}
                    catch (IllegalAccessException e) {throw new RuntimeException(e);}
                }
        );

        //capabilitiesMap.keySet().iterator().forEachRemaining(field -> log.new Important(field + ":" + capabilitiesMap.get(field)));

        for (String key : capabilities.getConfig(capabilities).keySet()) {

            log.new Info("Setting "+
                    PURPLE + key + GRAY +
                    " capability as: \"" +
                    PURPLE + capabilities.getConfig(capabilities).get(key) + GRAY +
                    "\" " + RESET
            );

            for (String capability: capabilitiesMap.keySet()) {
                if (key.equalsIgnoreCase(capabilitiesMap.get(capability))) {
                    log.new Warning("CAPABILITY: " +capability);
                    desiredCapabilities.setCapability(capability, capabilities.getConfig(capabilities).get(key));
                    log.new Success(key + " is set as " + capabilities.getConfig(capabilities).get(key));
                    break;
                }
            }
        }
        printObjectFields(desiredCapabilities);
        return desiredCapabilities;
    }

    public static void printObjectFields(Object object){
        List<Field> fields = List.of(object.getClass().getDeclaredFields());
        StringBuilder output = new StringBuilder();
        try {
            for (Field field:fields){
                field.setAccessible(true);
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
