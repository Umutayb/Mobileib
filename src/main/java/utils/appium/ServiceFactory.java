package utils.appium;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import utils.Printer;

import static resources.Colors.*;

public class ServiceFactory {
    static Printer log = new Printer(ServiceFactory.class);

    public static AppiumDriverLocalService service;
    static String address;
    static Integer port;

    public static void startService(String address, Integer port){
        log.new Info("Starting service on " + PURPLE + address + port + RESET);
        ServiceFactory.address = address;
        ServiceFactory.port = port;
        service = new AppiumServiceBuilder()
                .withIPAddress(address)
                .usingPort(port)
                .build();
        service.start();
    }

}
