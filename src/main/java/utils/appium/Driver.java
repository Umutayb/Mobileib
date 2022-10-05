package utils.appium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.webdriverextensions.WebComponent;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import models.Capabilities;
import utils.Printer;
import utils.PropertiesReader;
import utils.StringUtilities;
import utils.SystemUtilities;
import java.io.FileReader;
import java.io.IOException;

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

		String directory = properties.getProperty("config");//src/test/resources/configurations
		if (device==null) device = properties.getProperty("device");

		String address = properties.getProperty("address");
		int port = Integer.parseInt(properties.getProperty("port"));
		while (!new SystemUtilities().portIsAvailable(port)) port += 1;

		ServiceFactory.startService(address, port);	// Start Appium

		if(!Boolean.parseBoolean(properties.getProperty("detailed-logging")))ServiceFactory.service.clearOutPutStreams();

		assert directory != null;
		try(FileReader file = new FileReader(directory+"/"+device+".json")) {
			Capabilities capabilities = new ObjectMapper().readValue(file, Capabilities.class);
			driver = DriverFactory.getDriver(strUtils.firstLetterCapped(device), capabilities);
		}
		catch (IOException e) {log.new Warning(e.getMessage());}
		assert driver != null;
	}

	public void terminate(){
		log.new Info("Finalizing driver...");
		driver.quit();
		ServiceFactory.service.stop();
	}


}