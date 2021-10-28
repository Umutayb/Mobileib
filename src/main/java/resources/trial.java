package resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class trial {

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Capabilities obj = mapper.readValue(new File("src/main/java/resources/Capabilities.json"), Capabilities.class);
        System.out.println(obj.getConfig(obj));
    }
}
