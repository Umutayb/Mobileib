package utils.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.webdriverextensions.WebComponent;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.cucumber.core.api.Scenario;
import resources.Capabilities;
import utils.Printer;
import utils.PropertiesReader;
import utils.StringUtilities;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Driver extends WebComponent {

	public static AppiumDriver<MobileElement> driver;

	PropertiesReader reader = new PropertiesReader("properties-from-pom.properties");
	StringUtilities strUtils = new StringUtilities();
	Properties properties = new Properties();
	Printer log = new Printer(Driver.class);

	public void initialize() {
		log.new Info("Initializing driver");
		String device = reader.getProperty("device");

		try {properties.load(new FileReader("src/test/resources/test.properties"));}
		catch (IOException e) {log.new Warning(e.getMessage());}
		String directory = properties.getProperty("config");//src/test/resources/configurations
		if (device==null)
			device = properties.getProperty("device");

		assert directory != null;
		try(FileReader file = new FileReader(directory+"/"+device+".json")) {
			Capabilities capabilities = new ObjectMapper().readValue(file, Capabilities.class);
			driver = DriverFactory.getDriver(strUtils.firstLetterCapped(device), driver, capabilities);
		}
		catch (IOException e) {log.new Warning(e.getMessage());}
		assert driver != null;
	}

	public void terminate(Scenario scenario){
		log.new Info("Finalizing driver...");
		if (scenario.isFailed())
			log.captureScreen(scenario.getName()+"@"+scenario.getLine(),driver);
		driver.quit();
	}
}