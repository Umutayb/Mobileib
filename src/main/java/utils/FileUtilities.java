package utils;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static resources.Colors.*;

public class FileUtilities {

    Printer log = new Printer(FileUtilities.class);

    public String getString(String directory, String fileName) {
        try {return new String(Files.readAllBytes(Paths.get(directory+"/"+fileName)));}
        catch (IOException exception){
            Assert.fail(YELLOW+fileName+" not found");
            return null;
        }
    }

    public boolean verifyFilePresence(String fileDirectory) { //Verifies presence of a file at a given directory

        boolean fileIsPresent = false;

        try {
            File file = new File(fileDirectory);

            fileIsPresent = file.exists();

        } catch (Exception gamma) {

            gamma.printStackTrace();

        }
        return fileIsPresent;
    }
}
