package advisor;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Controller {
    private static final System.Logger LOGGER = System.getLogger("");
    private static final String CONFIG_FILE = "application.properties";

    private static final String RESPONSE_TYPE = "code";
    private static final String REDIRECT_URI;
    private static final String CLIENT_ID;
    private static final Properties properties = new Properties();

    static {
        final var isLoaded = getInternalProperties();
        LOGGER.log(INFO, "Is properties loaded: {0}", isLoaded);
        CLIENT_ID = properties.getProperty("CLIENT_ID");
        REDIRECT_URI = properties.getProperty("REDIRECT_URI");
        LOGGER.log(INFO, "Client ID: {0}", CLIENT_ID);
    }

    private static boolean getInternalProperties() {
        try (final var ins = Controller.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(ins);
            LOGGER.log(INFO, "Internal logger configuration is loaded successful.");
            return true;
        } catch (IOException e) {
            LOGGER.log(ERROR, "Could not load internal logger configuration: {0}", e.toString());
            return false;
        }
    }
}
