package utils.appium;

import com.github.webdriverextensions.WebComponent;
import io.appium.java_client.AppiumDriver;
import org.json.simple.JSONObject;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.*;
import java.io.IOException;
import java.net.ServerSocket;

import static utils.FileUtilities.properties;

@SuppressWarnings("unused")
public class Driver extends WebComponent {

	public static AppiumDriver driver;
	public static WebDriverWait wait;

	PropertiesReader reader = new PropertiesReader("properties-from-pom.properties");
	StringUtilities strUtils = new StringUtilities();
	Printer log = new Printer(Driver.class);

	public static void startService(){
		new Printer(Driver.class).new Info("Initializing appium service");

		String address = properties.getProperty("address");
		int port = Integer.parseInt(properties.getProperty("port"));

		if (!new SystemUtilities().portIsAvailable(port)){
			try (ServerSocket socket = new ServerSocket(0)) {port = socket.getLocalPort();}
			catch (IOException e) {throw new RuntimeException(e);}
		}

		ServiceFactory.startService(address, port);	// Start Appium

		if(!Boolean.parseBoolean(properties.getProperty("detailed-logging"))) ServiceFactory.service.clearOutPutStreams();
	}

	public void initialize() {
		log.new Info("Initializing appium driver");
		String device = reader.getProperty("device");
		if (device==null) device = properties.getProperty("device");

		String directory = properties.getProperty("config");//src/test/resources/configurations

		JSONObject json = DriverFactory.jsonUtils.parseJSONFile(directory+"/"+device+".json");
		driver = DriverFactory.getDriver(strUtils.firstLetterCapped(device), json);
	}

	public void terminate(){
		log.new Info("Finalizing driver...");
		try {driver.quit();}
		catch (Exception ignored){}
	}
}