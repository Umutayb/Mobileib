package utils;

import com.github.webdriverextensions.WebDriverExtensionFieldDecorator;
import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import com.gargoylesoftware.htmlunit.*;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import static resources.Colors.*;
import org.openqa.selenium.*;
import utils.driver.Driver;
import org.junit.Assert;
import java.util.List;

public abstract class Utilities extends Driver { //TODO: Write a method which creates a unique css selector for elements

    public Utilities(){PageFactory.initElements(new WebDriverExtensionFieldDecorator(driver), this);}

    Printer log = new Printer(Utilities.class);

    StringUtilities strUtils = new StringUtilities();
    NumericUtilities numeric = new NumericUtilities();

    public String getAttribute(MobileElement element, String attribute){return element.getAttribute(attribute);}

    public String navigate(String url){
        try {

            log.new info("Navigating to "+RESET+BLUE+url);

            if (!url.contains("http"))
                url = "https://"+url;

            driver.get(url);

        }catch (Exception gamma){

            Assert.fail(YELLOW+"Unable to navigate to the \""+url+"\""+RESET);
            driver.quit();

        }
        return url;
    }

    public void navigateBrowser(String direction){
        try {

            log.new info("Navigating "+direction);

            switch (direction.toLowerCase()){
                case "forward":
                    driver.navigate().forward();
                    break;

                case "backwards":
                    driver.navigate().back();
                    break;

                default:
                    Assert.fail(GRAY+"No such direction was defined in -navigateBrowser- method."+RESET);
            }

        }catch (Exception gamma){

            Assert.fail(YELLOW+"Unable to navigate \""+direction+"\""+RESET);
            driver.quit();

        }
    }

    //This method clicks an element after waiting it and scrolling it to the center of the view
    public void clickElement(MobileElement element){
        try {

            centerElement(waitUntilElementIsClickable(element, System.currentTimeMillis())).click();

        }catch (ElementNotFoundException e){
            Assert.fail(GRAY+e.getMessage()+RESET);
        }
    }

    //This method is for filling an input field, it waits for the element, scrolls to it, clears it and then fills it
    public void clearFillInput(MobileElement inputElement, String inputText, Boolean verify){
        try {
            // This method clears the input field before filling it
            clearInputField(centerElement(waitUntilElementIsVisible(inputElement, System.currentTimeMillis()))).sendKeys(inputText);

            if (verify)
                Assert.assertEquals(inputElement.getAttribute("value"), inputText);

        }catch (ElementNotFoundException e){
            Assert.fail(GRAY+e.getMessage()+RESET);
        }

    }

    public void hoverOver(MobileElement element, long startTime){
        if (System.currentTimeMillis()-startTime > 10000)
            return;
        Actions actions = new Actions(driver);
        try {
            actions.moveToElement(element)
                    .build()
                    .perform();
        }
        catch (StaleElementReferenceException stale){
            hoverOver(element, startTime);
        }
    }

    public void loopAndClick(List<MobileElement> list,String buttonName){
        for (MobileElement item:list) {
            if (item.getText().contains(buttonName)){
                click(item);
                return;
            }
        }
        Assert.fail(GRAY+"No button called "+buttonName+" was found."+RESET);
    }

    //This method clicks a button with a certain text on it
    public void clickButtonWithText(String buttonText){clickElement(getElementWithText(buttonText));}

    //This method clears an input field /w style
    public MobileElement clearInputField(MobileElement element){
        int textLength = element.getAttribute("value").length();
        for(int i = 0; i < textLength; i++){
            element.sendKeys(Keys.BACK_SPACE);
        }
        return element;
    }

    //This method returns an element with a certain text on it
    public MobileElement getElementWithText(String elementText){
        try {

            return driver.findElement(MobileBy.xpath("//*[text()='" +elementText+ "']"));

        }catch (ElementNotFoundException e){
            Assert.fail(GRAY+e.getMessage()+RESET);
            return null;
        }
    }

    //This method performs click, hold, drag and drop action on a certain element
    public void dragDropToAction(MobileElement element, MobileElement destinationElement){

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
    public void dragDropByAction(MobileElement element, int xOffset, int yOffset){

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
    public void dragDropAction(MobileElement element, int xOffset, int yOffset){

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
    public void clickAtAnOffset(MobileElement element, int xOffset, int yOffset){

        centerElement(element);

        Actions builder = new org.openqa.selenium.interactions.Actions(driver);
        builder.moveToElement(element, xOffset, yOffset)
                .click()
                .build()
                .perform();

    }

    //This method makes the thread wait for a certain while
    public void waitFor(double seconds){
        if (seconds > 1)
            log.new info("Waiting for "+BLUE+seconds+GRAY+" seconds");
        try {
            Thread.sleep((long) (seconds* 1000L));
        }
        catch (InterruptedException exception){
            Assert.fail(GRAY+exception.getLocalizedMessage()+RESET);
        }
    }

    //This method scrolls an element to the center of the view
    public MobileElement centerElement(MobileElement element){

        String scrollScript = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                + "var elementTop = arguments[0].getBoundingClientRect().top;"
                + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

        ((JavascriptExecutor) driver).executeScript(scrollScript, element);

        waitFor(0.5);

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
    public Object getElementObject(MobileElement element){
        return ((JavascriptExecutor) driver).executeScript("var items = {}; for (index = 0;" +
                        " index < arguments[0].attributes.length; ++index) " +
                        "{ items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;",
                element);
    }

    //This method prints all the attributes of a given element
    public void printElementAttributes(MobileElement element){
        JSONObject attributeJSON = new JSONObject(strUtils.str2Map(getElementObject(element).toString()));
        for (Object attribute : attributeJSON.keySet()) {
            log.new info(attribute +" : "+ attributeJSON.get(attribute));
        }
    }

    public MobileElement getParentByClass(MobileElement childElement, String current, String parentSelectorClass) {

        if (current == null) {
            current = "";
        }

        String childTag = childElement.getTagName();

        if (childElement.getAttribute("class").contains(parentSelectorClass))
            return childElement;

        MobileElement parentElement = childElement.findElement(MobileBy.xpath(".."));

        List<MobileElement> childrenElements = parentElement.findElements(MobileBy.xpath("*"));

        int count = 0;
        for (MobileElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) {
                count++;
            }
            if (childElement.equals(childrenElement)) {
                return getParentByClass(parentElement, "/" + childTag + "[" + count + "]" + current, parentSelectorClass);
            }

        }

        return null;

    }

    public String generateXPath(MobileElement childElement, String current) {
        String childTag = childElement.getTagName();
        if (childTag.equals("html")) {
            return "/html[1]" + current;
        }
        MobileElement parentElement = childElement.findElement(MobileBy.xpath(".."));
        List<MobileElement> childrenElements = parentElement.findElements(MobileBy.xpath("*"));
        int count = 0;
        for (MobileElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) {
                count++;
            }
            if (childElement.equals(childrenElement)) {
                return generateXPath(parentElement, "/" + childTag + "[" + count + "]" + current);
            }

        }

        return null;

    }

    public MobileElement waitUntilElementIsVisible(MobileElement element, long initialTime){
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        if (System.currentTimeMillis()-initialTime>15000){
            driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            return null;
        }
        try {
            if (!element.isDisplayed()){waitUntilElementIsVisible(element, initialTime);}
        }
        catch (StaleElementReferenceException|NoSuchElementException|TimeoutException exception){
            waitUntilElementIsVisible(element, initialTime);
        }
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        return element;
    }

    public List<MobileElement> verifyAbsenceOfElementLocatedBy(String locatorType, String locator, long startTime){

        List<MobileElement> elements;

        switch (locatorType.toLowerCase()){
            case "xpath":
                elements = driver.findElements(MobileBy.xpath(locator));
                break;

            case "css":
                elements = driver.findElements(MobileBy.cssSelector(locator));
                break;

            default:
                Assert.fail(GRAY+"No such locator type was defined in Helper.java @verifyAbsenceOfElementLocatedMobileBy."+RESET);
                return null;
        }

        if ((System.currentTimeMillis() - startTime) > 15000){
            Assert.fail(GRAY+"An element was located unexpectedly"+RESET);
            return elements;
        }

        if (elements.size() > 0){
            return verifyAbsenceOfElementLocatedBy(locatorType, locator, startTime);
        }
        else
            return null;
    }

    public void waitUntilElementIsNoLongerPresent(MobileElement element, long startTime){
        try {
            driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            List<MobileElement> elementPresence = driver.findElements(MobileBy.xpath(generateXPath(element,"")));
            while (elementPresence.size()>0){
                if ((System.currentTimeMillis() - startTime) > 15000)
                    Assert.fail(GRAY+"Element was still present after "+(System.currentTimeMillis() - startTime)/1000+" seconds."+RESET);
                elementPresence = driver.findElements(MobileBy.xpath(generateXPath(element,"")));
            }
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
        catch (StaleElementReferenceException exception) {
            if (System.currentTimeMillis()-startTime<=15000)
                waitUntilElementIsNoLongerPresent(element, startTime);
            else
                Assert.fail(GRAY+"Element was still present after "+(System.currentTimeMillis() - startTime)/1000+" seconds."+RESET);
        }
        catch (NoSuchElementException | IllegalArgumentException ignored){
            log.new success("The element is no longer present!");
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
    }

    public MobileElement waitUntilElementIsInvisible(MobileElement element, long startTime) {
        if ((System.currentTimeMillis() - startTime) > 15000)
            return element;
        try {
            wait.until(ExpectedConditions.invisibilityOf(element));
            return null;
        } catch (TimeoutException e) {
            return waitUntilElementIsInvisible(element, startTime);
        }
    }

    public MobileElement waitUntilElementIsClickable(MobileElement element, long initialTime){
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        if (System.currentTimeMillis()-initialTime>15000){
            driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            return null;
        }
        try {
            if (!element.isEnabled()){waitUntilElementIsClickable(element, initialTime);}
        }
        catch (StaleElementReferenceException|NoSuchElementException|TimeoutException exception){
            waitUntilElementIsClickable(element, initialTime);
        }
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        return element;
    }

    public void clickWithJS(MobileElement MobileElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", MobileElement);
    }

    public void scrollWithJS(MobileElement MobileElement) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", MobileElement);
    }

    public boolean elementIsDisplayed(MobileElement element, long startTime) {
        if ((System.currentTimeMillis() - startTime) > 10000)
            return false;
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            log.new info(e);
            return elementIsDisplayed(element, startTime);
        }
    }
}
