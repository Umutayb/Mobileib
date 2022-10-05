package models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import utils.FileUtilities;

import java.util.HashMap;
import java.util.Map;

public class Capabilities {
    public String platformName;
    public String platformVersion;
    public String deviceName;
    public String automationName;
    public String app;
    public String appActivity;
    public String appPackage;
    public String unexpectedAlertBehaviour;
    public String UDID;
    public String takesScreenshot;
    public String rotatable;
    public String orientation;
    public String locale;
    public String applicationName;
    public String appPath;
    public String language;
    public String avdName;
    public String noReset;
    public String reset;

    public Capabilities(){}

    public Map<String, String> getConfig(Capabilities capabilities){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> config = new HashMap<>();
        FileUtilities.Json jsonUtilities = new FileUtilities.Json();
        try {
            JSONObject configJson = jsonUtilities.str2json(mapper.writeValueAsString(capabilities));
            for (Object key:configJson.keySet()) {
                if (configJson.get(key)!=null)
                    config.put((String) key,(String)configJson.get(key));
            }
        }
        catch (JsonProcessingException e) {e.printStackTrace();}
        return config;
    }

    public enum Capability {
        DEVICE_NAME("DEVICENAME"),
        PLATFORM_NAME("PLATFORMNAME"),
        PLATFORM_VERSION("PLATFORMVERSION"),
        AUTOMATION_NAME("AUTOMATIONNAME"),
        APP("APP"),
        APP_ACTIVITY("APPACTIVITY"),
        APP_PACKAGE("APPPACKAGE"),
        UNEXPECTED_ALERT_BEHAVIOUR("UNEXPECTEDALERTBEHAVIOUR"),
        UDID("UDID"),
        TAKES_SCREENSHOT("TAKESSCREENSHOT"),
        ROTATABLE("ROTATABLE"),
        ORIENTATION("ORIENTATION"),
        LOCALE("LOCALE"),
        APPLICATION_NAME("APPLICATIONNAME"),
        APP_PATH("APPPATH"),
        LANGUAGE("LANGUAGE"),
        AVD_NAME("AVDNAME"),
        NO_RESET("NORESET"),
        RESET("RESET"),
        UI_AUTOMATOR_2_SERVER_INSTALL_TIMEOUT("UIAUTOMATOR2SERVERINSTALLTIMEOUT");

        final String value;

        Capability(String value){this.value = value;}
    }
}