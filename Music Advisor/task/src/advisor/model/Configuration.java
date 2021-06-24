package advisor.model;

import java.io.IOException;
import java.util.Properties;

import static java.lang.System.Logger.Level.*;

public class Configuration {
    public static final String CONFIG_FILE = "application.properties";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String RESPONSE_TYPE = "code";
    private static final System.Logger LOGGER = System.getLogger("");
    private final String redirectHost;
    private final int redirectPort;
    private final String clientId;
    private final String clientSecret;
    private String authServer;
    private String apiServer;
    private int page;
    private String accessToken;

    public Configuration(String[] args) {
        final var properties = getProperties(CONFIG_FILE);

        clientId = properties.getProperty("client.id");
        clientSecret = properties.getProperty("client.secret");
        redirectHost = properties.getProperty("redirect.host", "http://localhost");
        redirectPort = Integer.parseInt(properties.getProperty("redirect.port", "8080"));

        authServer = properties.getProperty("authentication.uri", "https://accounts.spotify.com");
        apiServer = properties.getProperty("api.uri", "https://api.spotify.com");
        page = Integer.parseInt(properties.getProperty("page", "5"));

        parseArguments(args);

        LOGGER.log(INFO, "Authentication server: {0}", authServer);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    private Properties getProperties(String fileName) {
        final var properties = new Properties();

        try (final var ins = Configuration.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(ins);
            LOGGER.log(INFO, "Internal logger configuration is loaded successful.");
        } catch (IOException e) {
            LOGGER.log(ERROR, "Could not load internal logger configuration: {0}", e.toString());
        }
        return properties;
    }

    private void parseArguments(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-access":
                    authServer = args[++i];
                    break;
                case "-resource":
                    apiServer = args[++i];
                    break;
                case "-page":
                    page = Integer.parseInt(args[++i]);
                    break;
                default:
                    LOGGER.log(WARNING, "Unknown property {0}", args[i]);
            }
        }
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
