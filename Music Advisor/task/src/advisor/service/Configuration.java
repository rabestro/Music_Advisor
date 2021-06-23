package advisor.service;

import advisor.Controller;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Configuration {
    private static final System.Logger LOGGER = System.getLogger("");
    private static final Properties properties = new Properties();

    public static final String AUTH_SERVER = "https://accounts.spotify.com";
    public static final String API_SERVER = "https://api.spotify.com";
    public static final String CONFIG_FILE = "application.properties";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String RESPONSE_TYPE = "code";
    public static final String CLIENT_SECRET;
    public static final String REDIRECT_URI;
    public static final String CLIENT_ID;

    static {
        final var isLoaded = getInternalProperties();
        LOGGER.log(INFO, "Is properties loaded: {0}", isLoaded);
        CLIENT_ID = properties.getProperty("CLIENT_ID");
        REDIRECT_URI = properties.getProperty("REDIRECT_URI");
        CLIENT_SECRET = properties.getProperty("CLIENT_SECRET");

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
