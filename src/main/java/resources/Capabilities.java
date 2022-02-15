package resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

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
        JsonUtilities jsonUtils = new JsonUtilities();
        try {
            JSONObject configJson = jsonUtils.str2json(mapper.writeValueAsString(capabilities));
            for (Object key:configJson.keySet()) {
                if (configJson.get(key)!=null)
                    config.put((String) key,(String)configJson.get(key));
            }
        }
        catch (JsonProcessingException e) {e.printStackTrace();}
        return config;
    }

}