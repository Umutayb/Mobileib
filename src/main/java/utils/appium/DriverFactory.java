package utils.appium;

import com.google.gson.JsonObject;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.FileUtilities;
import utils.Printer;
import utils.StringUtilities;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

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
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        for (String key : capabilities.keySet()) desiredCapabilities.setCapability(key, capabilities.get(key));

        log.new Success("Capabilities are successfully set as:");
        log.new Info(jsonUtils.formatJsonString(capabilities.getAsString()));

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
