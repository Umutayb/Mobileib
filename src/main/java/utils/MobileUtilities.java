package utils;

import com.github.webdriverextensions.WebDriverExtensionFieldDecorator;
import context.ContextStore;
import io.appium.java_client.AppiumDriver;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import com.gargoylesoftware.htmlunit.*;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import org.json.simple.JSONObject;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static resources.Colors.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import resources.Colors;
import utils.appium.Driver;
import org.junit.Assert;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MobileUtilities extends Driver { //TODO: Write a method which creates a unique css selector for elements

    public Printer log = new Printer(this.getClass());

    public TextParser parser = new TextParser();
    public StringUtilities strUtils = new StringUtilities();
    public ObjectUtilities objectUtils = new ObjectUtilities();

    public enum Color {CYAN, RED, GREEN, YELLOW, PURPLE, GRAY, BLUE}
    public enum ElementState {ENABLED, DISPLAYED, SELECTED, DISABLED, UNSELECTED, ABSENT}
    public enum Direction {UP, DOWN, LEFT, RIGHT}
    public enum Navigation {BACKWARDS, FORWARDS}
    public enum Locator {XPATH, CSS}

    public Properties properties;

    public long elementTimeout;

    public MobileUtilities(){
        PageFactory.initElements(new WebDriverExtensionFieldDecorator(driver), this);
        properties = FileUtilities.properties;
        elementTimeout = Long.parseLong(properties.getProperty("element-timeout", "15000"));
    }

    public String getAttribute(WebElement element, String attribute){return element.getAttribute(attribute);}

    public WebElement getElementFromPage(String elementFieldName, String pageName, Object objectRepository){
        Map<String, Object> pageFields = objectUtils.getFields(objectUtils.getFields(objectRepository).get(pageName));
        return (WebElement) pageFields.get(elementFieldName);
    }

    public Map<String, Object> getComponentFieldsFromPage(String componentName, String pageName, Object objectRepository){
        Map<String, Object> pageFields = objectUtils.getFields(objectUtils.getFields(objectRepository).get(pageName));
        return objectUtils.getFields(pageFields.get(componentName));
    }

    public WebElement getElementFromComponent(String elementFieldName, String componentName, String pageName, Object objectRepository){
        return (WebElement) getComponentFieldsFromPage(componentName, pageName, objectRepository).get(elementFieldName);
    }

    @SuppressWarnings("unchecked")
    public List<WebElement> getElementsFromPage(String elementFieldName, String pageName, Object objectRepository){
        Map<String, Object> pageFields = objectUtils.getFields(objectUtils.getFields(objectRepository).get(pageName));
        return (List<WebElement>) pageFields.get(elementFieldName);
    }

    @SuppressWarnings("unchecked")
    public List<WebElement> getElementsFromComponent(String elementFieldName, String componentName, String pageName, Object objectRepository){
        return (List<WebElement>) getComponentFieldsFromPage(componentName, pageName, objectRepository).get(elementFieldName);
    }

    public String navigate(String url){
        try {
            log.new Info("Navigating to "+RESET+BLUE+url+RESET);

            if (!url.contains("http")) url = "https://"+url;

            driver.get(url);
        }
        catch (Exception gamma){
            Assert.fail("Unable to navigate to the \""+highlighted(Color.YELLOW, url)+"\"");
            driver.quit();
        }
        return url;
    }

    public void setWindowSize(Integer width, Integer height) {
        driver.manage().window().setSize(new Dimension(width,height));
    }

    public String highlighted(Color color, String text){return (objectUtils.getFieldValue(color.name(), Colors.class) + text + RESET);}

    public void navigateBrowser(Navigation direction){
        try {
            log.new Info("Navigating "+highlighted(Color.BLUE, direction.name()));

            switch (direction){
                case FORWARDS:
                    driver.navigate().forward();
                    break;

                case BACKWARDS:
                    driver.navigate().back();
                    break;

                default:
                    throw new EnumConstantNotPresentException(Navigation.class, direction.name());
            }
        }
        catch (Exception e){
            Assert.fail("Unable to navigate browser \""+highlighted(Color.YELLOW, direction.name())+"\" due to: " + e);
        }
    }

    //This method clicks an element after waiting sit and scrolling it to the center of the view
    public void clickElement(WebElement element, Boolean scroll){
        long initialTime = System.currentTimeMillis();
        WebDriverException caughtException = null;
        boolean timeout;
        int counter = 0;
        waitUntilElementIs(element, ElementState.ENABLED, false);
        do {
            timeout = System.currentTimeMillis()-initialTime > elementTimeout;
            try {
                if (scroll) centerElement(element).click();
                else element.click();
                return;
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException;
                }
                else if (!webDriverException.getClass().getName().equals(caughtException.getClass().getName())){
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException;
                }
                counter++;
            }
        }
        while (!timeout);
        if (counter > 0) log.new Warning("Iterated " + counter + " time(s)!");
        log.new Warning(caughtException.getMessage());
        throw new RuntimeException(caughtException);
    }

    public void clearFillInput(WebElement inputElement, String inputText, @NotNull Boolean scroll, Boolean verify){
        // This method clears the input field before filling it
        if (scroll) clearInputField(centerElement(waitUntilElementIs(inputElement, ElementState.DISPLAYED, false)))
                .sendKeys(inputText);
        else clearInputField(waitUntilElementIs(inputElement, ElementState.DISPLAYED, false))
                .sendKeys(inputText);

        if (verify) Assert.assertEquals(inputText, inputElement.getAttribute("value"));
    }

    public WebElement waitUntilElementIs(WebElement element, ElementState state, @NotNull Boolean strict){
        if (strict) Assert.assertTrue("Element is not in " + state.name() + " state!", elementIs(element, state));
        else elementIs(element, state);
        return element;
    }

    public Boolean elementIs(WebElement element, @NotNull ElementState state){
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        long initialTime = System.currentTimeMillis();
        String caughtException = null;
        boolean timeout;
        boolean condition = false;
        boolean negativeCheck = false;
        int counter = 0;
        do {
            timeout = System.currentTimeMillis()-initialTime > elementTimeout;
            if (condition) return true;
            else if (counter > 1 && negativeCheck) return true;
            try {
                switch (state){
                    case ENABLED:
                        negativeCheck = false;
                        condition = element.isEnabled();
                        break;

                    case DISPLAYED:
                        negativeCheck = false;
                        condition = element.isDisplayed();
                        break;

                    case SELECTED:
                        negativeCheck = false;
                        condition = element.isSelected();
                        break;

                    case DISABLED:
                        negativeCheck = true;
                        condition = !element.isEnabled();
                        break;

                    case UNSELECTED:
                        negativeCheck = true;
                        condition = !element.isSelected();
                        break;

                    case ABSENT:
                        negativeCheck = true;
                        condition = !element.isDisplayed();
                        break;

                    default: throw new EnumConstantNotPresentException(ElementState.class, state.name());
                }
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                else if (!webDriverException.getClass().getName().equals(caughtException)){
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                }
                counter++;
            }
        }
        while (!timeout);
        if (counter > 0) log.new Warning("Iterated " + counter + " time(s)!");
        return false;
    }

    public WebElement hoverOver(WebElement element){
        long initialTime = System.currentTimeMillis();
        Actions actions = new Actions(driver);
        String caughtException = null;
        boolean timeout;
        int counter = 0;
        do {
            try {
                centerElement(element);
                actions.moveToElement(element).build().perform();
                break;
            }
            catch (WebDriverException webDriverException){
                if (counter == 0) {
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                    counter++;
                }
                else if (!webDriverException.getClass().getName().equals(caughtException)){
                    log.new Warning("Iterating... (" + webDriverException.getClass().getName() + ")");
                    caughtException = webDriverException.getClass().getName();
                    counter++;
                }
                timeout = System.currentTimeMillis()-initialTime > elementTimeout;
            }
        }
        while (timeout);
        return element;
    }

    public void loopAndClick(List<WebElement> list, String buttonName, Boolean scroll){
        clickElement(acquireNamedElementAmongst(list,buttonName), scroll);
    }

    public <T> T acquireNamedComponentAmongst(List<T> items, String selectionName){
        log.new Info("Acquiring component called " + highlighted(Color.BLUE, selectionName));
        boolean timeout = false;
        long initialTime = System.currentTimeMillis();
        while (!timeout){
            for (T selection : items) {
                String text = ((WebElement) selection).getText();
                if (text.equalsIgnoreCase(selectionName) || text.contains(selectionName)) return selection;
            }
            if (System.currentTimeMillis() - initialTime > elementTimeout) timeout = true;
        }
        throw new NoSuchElementException("No component with text/name '" + selectionName + "' could be found!");
    }

    public WebElement acquireNamedElementAmongst(List<WebElement> items, String selectionName){
        log.new Info("Acquiring element called " + highlighted(Color.BLUE, selectionName));
        boolean timeout = false;
        long initialTime = System.currentTimeMillis();
        while (!timeout){
            for (WebElement selection : items) {
                String name = selection.getAccessibleName();
                String text = selection.getText();
                if (    name.equalsIgnoreCase(selectionName) ||
                        name.contains(selectionName)         ||
                        text.equalsIgnoreCase(selectionName) ||
                        text.contains(selectionName)
                ) return selection;
            }
            if (System.currentTimeMillis() - initialTime > elementTimeout) timeout = true;
        }
        throw new NoSuchElementException("No element with text/name '" + selectionName + "' could be found!");
    }

    public WebElement acquireElementUsingAttributeAmongst(List<WebElement> items, String attributeName, String attributeValue){
        log.new Info("Acquiring element called " + highlighted(Color.BLUE, attributeValue) + " using its " + highlighted(Color.BLUE, attributeName) + " attribute");
        boolean condition = true;
        long initialTime = System.currentTimeMillis();
        while (condition){
            for (WebElement selection : items) {
                String attribute = selection.getAttribute(attributeName);
                if (attribute.equalsIgnoreCase(attributeValue) || attribute.contains(attributeValue)) return selection;
            }
            if (System.currentTimeMillis() - initialTime > elementTimeout) condition = false;
        }
        throw new NoSuchElementException("No element with the attributes '" + attributeName + " : " + attributeValue + "' could be found!");
    }

    @Deprecated(since = "0.5.5", forRemoval = true)
    public WebElement waitUntilElementIsVisible(WebElement element, long initialTime){
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        try {if (!element.isDisplayed()){throw new InvalidElementStateException("Element is not displayed!");}}
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis()-initialTime>15000)){
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                waitUntilElementIsVisible(element, initialTime);
            }
            else throw new NoSuchElementException("The element could not be located!");
        }
        return element;
    }

    public String switchWindowHandle(String handle){
        String parentWindowHandle = driver.getWindowHandle();
        if (handle == null)
            for (String windowHandle:driver.getWindowHandles()) {
                if (!windowHandle.equalsIgnoreCase(parentWindowHandle))
                    driver = (AppiumDriver) driver.switchTo().window((windowHandle));
            }
        else driver = (AppiumDriver) driver.switchTo().window(handle);
        return parentWindowHandle;
    }

    public void clickButtonWithText(String buttonText, Boolean scroll){clickElement(getElementByText(buttonText), scroll);}

    public WebElement clearInputField(@NotNull WebElement element){
        int textLength = element.getAttribute("value").length();
        for(int i = 0; i < textLength; i++){element.sendKeys(Keys.BACK_SPACE);}
        return element;
    }

    public WebElement getElementByText(String elementText){
        try {
            return driver.findElement(By.xpath("//*[text()='" +elementText+ "']"));
        }
        catch (ElementNotFoundException | NoSuchElementException exception){
            throw new NoSuchElementException(GRAY+exception.getMessage()+RESET);
        }
    }

    public WebElement getElementContainingText(String elementText){
        try {
            return driver.findElement(By.xpath("//*[contains(text(), '" +elementText+ "')]"));
        }
        catch (ElementNotFoundException | NoSuchElementException exception){
            throw new NoSuchElementException(GRAY+exception.getMessage()+RESET);
        }
    }

    public void clickAtAnOffset(WebElement element, int xOffset, int yOffset, boolean scroll){

        if (scroll) centerElement(element);

        Actions builder = new org.openqa.selenium.interactions.Actions(driver);
        builder
                .moveToElement(element, xOffset, yOffset)
                .click()
                .build()
                .perform();
    }

    public void uploadFile(@NotNull WebElement fileUploadInput, String directory, String fileName){fileUploadInput.sendKeys(directory+"/"+fileName);}

    public void waitFor(double seconds){
        if (seconds > 1) log.new Info("Waiting for "+BLUE+seconds+GRAY+" seconds");
        try {Thread.sleep((long) (seconds* 1000L));}
        catch (InterruptedException exception){Assert.fail(GRAY+exception.getLocalizedMessage()+RESET);}
    }

    @Deprecated(since = "0.5.5", forRemoval = true)
    public void waitAndClickIfElementIsClickable(WebElement element, Boolean scroll, long initialTime){
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        try {
            if (!element.isEnabled()){throw new InvalidElementStateException("Element is not enabled!");}
            else {
                if (scroll) centerElement(element).click();
                else element.click();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            }
        }
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis()-initialTime>15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                waitAndClickIfElementIsClickable(element, scroll, initialTime);
            }
            else throw exception;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public WebElement hoverOver(WebElement element, Long initialTime){
        if (System.currentTimeMillis()-initialTime > 10000) return null;
        centerElement(element);
        Actions actions = new Actions(driver);
        try {actions.moveToElement(element).build().perform();}
        catch (WebDriverException ignored) {hoverOver(element,initialTime);}
        return element;
    }

    public WebElement acquireNamedElementAmongst(List<WebElement> items, String selectionName, long initialTime){
        log.new Info("Acquiring element called " + highlighted(Color.BLUE, selectionName));
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        try {
            for (WebElement selection : items) {
                String name = selection.getAccessibleName();
                String text = selection.getText();
                if (
                        name.equalsIgnoreCase(selectionName) ||
                                name.contains(selectionName)         ||
                                text.equalsIgnoreCase(selectionName) ||
                                text.contains(selectionName)
                ) return selection;
            }
            throw new NoSuchElementException("No element with text/name '" + selectionName + "' could be found!");
        }
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis()-initialTime>15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                return acquireNamedElementAmongst(items, selectionName, initialTime);
            }
            throw exception;
        }
    }

    public <T> T acquireNamedComponentAmongst(List<T> items, String selectionName, long initialTime){
        log.new Info("Acquiring element called " + highlighted(Color.BLUE, selectionName));
        try {
            for (T selection : items) {
                String text = ((WebElement) selection).getText();
                if (text.equalsIgnoreCase(selectionName) || text.contains(selectionName)) return selection;
            }
            throw new NoSuchElementException("No component with text/name '" + selectionName + "' could be found!");
        }
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis()-initialTime>15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                return acquireNamedComponentAmongst(items, selectionName, initialTime);
            }
            throw exception;
        }
    }

    public WebElement acquireElementUsingAttributeAmongst(List<WebElement> elements, String attributeName, String attributeValue, long initialTime){
        log.new Info("Acquiring element called " + highlighted(Color.BLUE, attributeValue) + " using its " + highlighted(Color.BLUE, attributeName) + " attribute");
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        try {
            for (WebElement selection : elements) {
                String attribute = selection.getAttribute(attributeName);
                if (attribute.equalsIgnoreCase(attributeValue) || attribute.contains(attributeValue)) return selection;
            }
            throw new NoSuchElementException("No element with the attributes '" + attributeName + " : " + attributeValue + "' could be found!");
        }
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis()-initialTime>15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                return acquireElementUsingAttributeAmongst(elements, attributeName, attributeValue, initialTime);
            }
            throw exception;
        }
    }

    //This method returns an element with a certain text on it
    public WebElement getElementByText(String elementText, long initialTime){
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        WebElement element;
        try {
            element = driver.findElement(By.xpath("//*[text()='" +elementText+ "']"));
            if (!element.isEnabled()){throw new InvalidElementStateException("Element is not enabled!");}
            else {
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
                return element;
            }
        }
        catch (WebDriverException exception){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            if (!(System.currentTimeMillis() - initialTime>15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                return getElementByText(elementText, initialTime);
            }
            else throw exception;
        }
    }

    //This method performs click, hold, drag and drop action on a certain element
    public void dragDropToAction(WebElement element, WebElement destinationElement){

        centerElement(element);

        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .moveToElement(destinationElement)
                .release()
                .build()
                .perform();
        waitFor(0.5);
    }

    //This method performs click, hold, dragAndDropBy action on at a certain offset
    public void dragDropByAction(WebElement element, int xOffset, int yOffset){

        centerElement(element);

        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .dragAndDropBy(element, xOffset, yOffset)
                .build()
                .perform();
        waitFor(0.5);
    }

    //This method performs click, hold, drag and drop action on at a certain offset
    public void dragDropAction(WebElement element, int xOffset, int yOffset){

        centerElement(element);

        Actions action = new Actions(driver);
        action.moveToElement(element)
                .clickAndHold(element)
                .moveToElement(element,xOffset,yOffset)
                .release()
                .build()
                .perform();
        waitFor(0.5);
    }

    //This method refreshes the current page
    public void refreshThePage(){driver.navigate().refresh();}

    //This method clicks an element at an offset
    public void clickAtAnOffset(WebElement element, int xOffset, int yOffset){

        centerElement(element);

        Actions builder = new org.openqa.selenium.interactions.Actions(driver);
        builder
                .moveToElement(element, xOffset, yOffset)
                .click()
                .build()
                .perform();
    }

    public Alert getAlert(){return driver.switchTo().alert();}

    public String combineKeys(Keys key1, Keys key2){return Keys.chord(key1,key2);}

    //This method scrolls an element to the center of the view
    //This method scrolls an element to the center of the view
    public WebElement centerElement(WebElement element){

        Point center = new Point(
                driver.manage().window().getSize().getWidth()/2,
                driver.manage().window().getSize().getHeight()/2
        );

        int verticalScrollDist = element.getLocation().getY() - driver.manage().window().getSize().getHeight()/2;
        int verticalScrollStep = driver.manage().window().getSize().getHeight()/3;

        int horizontalScrollDist = element.getLocation().getX() - driver.manage().window().getSize().getWidth()/2;
        int horizontalScrollStep = driver.manage().window().getSize().getWidth()/3;
        for (int i = 0; i <= verticalScrollDist / verticalScrollStep; i++) {
            if (i == verticalScrollDist / verticalScrollStep){
                swipeFromCenter(new Point(
                        center.getX() + horizontalScrollDist % horizontalScrollStep,
                        center.getY() + verticalScrollDist % verticalScrollStep));
            }
            else{
                swipeFromCenter(new Point(
                        center.getX() + horizontalScrollStep,
                        center.getY() + verticalScrollStep));
            }
        }

        return element;
    }

    public void swiper(Direction direction){
        Point center = new Point(
                driver.manage().window().getSize().getWidth()/2,
                driver.manage().window().getSize().getHeight()/2
        );

        Point destination;
        switch (direction){
            case UP:
                destination = new Point(
                        center.getX(),
                        center.getY() + 3*driver.manage().window().getSize().getHeight()/4
                );
                break;

            case DOWN:
                destination = new Point(
                        center.getX(),
                        center.getY() - 3*driver.manage().window().getSize().getHeight()/4
                );
                break;

            case LEFT:
                destination = new Point(
                        center.getX()-3*driver.manage().window().getSize().getWidth()/4,
                        center.getY()
                );
                break;

            case RIGHT:
                destination = new Point(
                        center.getX()+3*driver.manage().window().getSize().getWidth()/4,
                        center.getY()
                );
                break;

            default:
                destination = null;
                Assert.fail(YELLOW+"No such swipe direction was defined in horizontal swipe.");
        }
        swipe(center, destination);
    }

    public void swipe(Point pointOfDeparture, Point pointOfArrival){
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1);
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(0),
                PointerInput.Origin.viewport(), pointOfDeparture.x, pointOfDeparture.y));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.MIDDLE.asArg()));
        sequence.addAction(new Pause(finger, ofMillis(750)));
        sequence.addAction(finger.createPointerMove(ofMillis(250),
                PointerInput.Origin.viewport(), pointOfArrival.x, pointOfArrival.y));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.MIDDLE.asArg()));
        performSequence(sequence, System.currentTimeMillis());
    }

    public void performSequence(Sequence sequence, long initialTime){
        try {driver.perform(singletonList(sequence));}
        catch (WebDriverException exception){
            if (!(System.currentTimeMillis() - initialTime > 15000)) {
                log.new Warning("Recursion! (" + exception.getClass().getName() + ")");
                performSequence(sequence, initialTime);
            }
            else throw exception;
        }
    }

    public void swipeFromCenter(Point point){
        Point center = new Point(
                driver.manage().window().getSize().getWidth()/2,
                driver.manage().window().getSize().getHeight()/2);
        swipe(center, point);
    }

    public WebElement swipeElement(WebElement element, Point point){
        Point center = new Point(element.getLocation().x, element.getLocation().y);
        swipe(center, point);
        return element;
    }

    public WebElement swipeWithOffset(WebElement element, Integer xOffset, Integer yOffset){
        Point from = new Point(element.getLocation().x, element.getLocation().y);
        Point to = new Point(element.getLocation().x + xOffset, element.getLocation().y + yOffset);
        swipe(from, to);
        return element;
    }

    public WebElement swipeFromTo(WebElement element, WebElement destinationElement){
        Point from = new Point(element.getLocation().x, element.getLocation().y);
        Point to = new Point(destinationElement.getLocation().x, destinationElement.getLocation().y);
        swipe(from, to);
        return element;
    }

    //This method verifies current url
    public void verifyUrl(String url){
        Assert.assertTrue(driver.getCurrentUrl().contains(url));
    }

    //This method verifies the page title
    public void verifyPageTitle(String pageTitle){
        Assert.assertTrue(driver.getTitle().contains(pageTitle));
    }

    //This method returns all the attributes of an element as an object
    public Object getElementObject(WebElement element){
        return ((JavascriptExecutor) driver).executeScript("var items = {}; for (index = 0;" +
                        " index < arguments[0].attributes.length; ++index) " +
                        "{ items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;",
                element
        );
    }

    //This method prints all the attributes of a given element
    public void printElementAttributes(WebElement element){
        JSONObject attributeJSON = new JSONObject(strUtils.str2Map(getElementObject(element).toString()));
        for (Object attribute : attributeJSON.keySet()) log.new Info(attribute +" : "+ attributeJSON.get(attribute));
    }

    public WebElement getParentByClass(WebElement childElement, String current, String parentSelectorClass) {

        if (current == null) {current = "";}

        String childTag = childElement.getTagName();

        if (childElement.getAttribute("class").contains(parentSelectorClass)) return childElement;

        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));

        int count = 0;
        for (WebElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) count++;
            if (childElement.equals(childrenElement)) {
                return getParentByClass(parentElement, "/" + childTag + "[" + count + "]" + current, parentSelectorClass);
            }
        }
        return null;
    }

    public String generateXPath(WebElement childElement, String current) {
        String childTag = childElement.getTagName();
        if (childTag.equals("html")) {return "/html[1]" + current;}
        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
        int count = 0;
        for (WebElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) count++;
            if (childElement.equals(childrenElement)) {
                return generateXPath(parentElement, "/" + childTag + "[" + count + "]" + current);
            }
        }
        return null;
    }

    public List<WebElement> verifyAbsenceOfElementLocatedBy(Locator locatorType, String locator, long startTime){

        List<WebElement> elements;

        switch (locatorType){
            case XPATH:
                elements = driver.findElements(By.xpath(locator));
                break;

            case CSS:
                elements = driver.findElements(By.cssSelector(locator));
                break;

            default: throw new EnumConstantNotPresentException(Locator.class, locatorType.name());
        }

        if ((System.currentTimeMillis() - startTime) > 15000){
            Assert.fail(GRAY+"An element was located unexpectedly"+RESET);
            return elements;
        }
        if (elements.size() > 0){return verifyAbsenceOfElementLocatedBy(locatorType, locator, startTime);}
        else return null;
    }

    public void waitUntilElementIsNoLongerPresent(WebElement element, long startTime){
        try {
            WebDriver subDriver = driver;
            subDriver.manage().timeouts().implicitlyWait(ofMillis(500));
            List<WebElement> elementPresence = driver.findElements(By.xpath(generateXPath(element,"")));
            while (elementPresence.size()>0){
                if ((System.currentTimeMillis() - startTime) > 15000)
                    throw new TimeoutException(GRAY+"Element was still present after "+(System.currentTimeMillis() - startTime)/1000+" seconds."+RESET);
                elementPresence = subDriver.findElements(By.xpath(generateXPath(element,"")));
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        }
        catch (WebDriverException exception){
            if (System.currentTimeMillis()-startTime<=15000) waitUntilElementIsNoLongerPresent(element, startTime);
            else throw new TimeoutException(GRAY+"Element was still present after "+(System.currentTimeMillis() - startTime)/1000+" seconds."+RESET);
        }
        catch (IllegalArgumentException ignored){
            log.new Success("The element is no longer present!");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        }
    }

    public WebElement waitUntilElementIsInvisible(WebElement element, long startTime) {
        if ((System.currentTimeMillis() - startTime) > 15000) return element;
        try {
            wait.until(ExpectedConditions.invisibilityOf(element));
            return null;
        }
        catch (TimeoutException e) {return waitUntilElementIsInvisible(element, startTime);}
    }

    public WebElement waitUntilElementIsClickable(WebElement element, long initialTime){
        driver.manage().timeouts().implicitlyWait(ofMillis(500));
        if (System.currentTimeMillis()-initialTime>15000){
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            return null;
        }
        try {if (!element.isEnabled()){waitUntilElementIsClickable(element, initialTime);}}
        catch (WebDriverException exception){
            return waitUntilElementIsClickable(element, initialTime);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        return element;
    }

    public void clickWithJS(WebElement webElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", webElement);
    }

    public void scrollWithJS(WebElement webElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", webElement);
    }

    public boolean elementIsDisplayed(WebElement element, long startTime) {
        if ((System.currentTimeMillis() - startTime) > 10000) return false;
        try {return element.isDisplayed();}
        catch (Exception e) {
            log.new Info(e);
            return elementIsDisplayed(element, startTime);
        }
    }

    public <T> WebElement getElement(String fieldName, Class<T> inputClass){
        return (WebElement) objectUtils.getFieldValue(fieldName, inputClass);
    }

    public String contextCheck(String input){
        if (input.contains("CONTEXT-"))
            input = ContextStore.get(new TextParser().parse("CONTEXT-", null, input)).toString();
        if (input.contains("RANDOM-")){
            boolean useLetters = input.contains("LETTER");
            boolean useNumbers = input.contains("NUMBER");
            String keyword = "";
            if (input.contains("KEYWORD")) keyword = new TextParser().parse("-K=", "-", input);
            int length = Integer.parseInt(new TextParser().parse("-L=", null, input));
            input = strUtils.generateRandomString(keyword, length, useLetters, useNumbers);
        }
        return input;

    }
}
