package advisor;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Configuration {
    public static final String API_SERVER = "https://api.spotify.com";
    public static final String CONFIG_FILE = "application.properties";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String RESPONSE_TYPE = "code";
    private static final System.Logger LOGGER = System.getLogger("");
    private final String authServer;
    private final String redirectHost;
    private final int redirectPort;
    private final String clientId;
    private final String clientSecret;

    public Configuration(String[] args) {
        final var properties = new Properties();

        try (final var ins = Configuration.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(ins);
            LOGGER.log(INFO, "Internal logger configuration is loaded successful.");
        } catch (IOException e) {
            LOGGER.log(ERROR, "Could not load internal logger configuration: {0}", e.toString());
        }

        if (args.length > 1 && args[0].equals("-access")) {
            authServer = args[1];
        } else {
            authServer = properties.getProperty("authentication.uri", "https://accounts.spotify.com");
        }
        LOGGER.log(INFO, "Authentication server: {0}", authServer);
        clientId = properties.getProperty("client.id");
        clientSecret = properties.getProperty("client.secret");
        redirectHost = properties.getProperty("redirect.host", "http://localhost");
        redirectPort = Integer.parseInt(properties.getProperty("redirect.port", "8080"));

    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectHost + ":" + redirectPort + "";
    }

    public String getRedirectHost() {
        return redirectHost;
    }

    public int getRedirectPort() {
        return redirectPort;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAuthServer() {
        return authServer;
    }

    public String getAuthLink() {
        return authServer + "/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectHost + ":" + redirectPort
                + "&response_type=code";
    }

    public int getPort() {
        return redirectPort;
    }
}
