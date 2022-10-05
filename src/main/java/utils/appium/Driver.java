package utils.appium;

import com.github.webdriverextensions.WebComponent;
import com.google.gson.JsonObject;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.*;

import static utils.FileUtilities.properties;

@SuppressWarnings("unused")
public class Driver extends WebComponent {

	public static AppiumDriver driver;
	public static WebDriverWait wait;

	PropertiesReader reader = new PropertiesReader("properties-from-pom.properties");
	StringUtilities strUtils = new StringUtilities();
	Printer log = new Printer(Driver.class);

	public void initialize() {
		log.new Info("Initializing appium service & driver");
		String device = reader.getProperty("device");
		if (device==null) device = properties.getProperty("device");

		String directory = properties.getProperty("config");//src/test/resources/configurations

		String address = properties.getProperty("address");
		int port = Integer.parseInt(properties.getProperty("port"));
		while (!new SystemUtilities().portIsAvailable(port)) port += 1;

		ServiceFactory.startService(address, port);	// Start Appium

		if(!Boolean.parseBoolean(properties.getProperty("detailed-logging")))ServiceFactory.service.clearOutPutStreams();

		JsonObject json = DriverFactory.jsonUtils.parseJsonFile(directory+"/"+device+".json");
		driver = DriverFactory.getDriver(strUtils.firstLetterCapped(device), json);
	}

	public void terminate(){
		log.new Info("Finalizing driver...");
		try {driver.quit();}
		catch (Exception ignored){}
		finally {ServiceFactory.service.stop();}
	}
}