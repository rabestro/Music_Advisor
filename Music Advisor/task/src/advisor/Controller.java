package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.net.URI;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Controller {
    private static final System.Logger LOGGER = System.getLogger("");
    private static final String AUTH_SERVER = "https://accounts.spotify.com";
    private static final String API_SERVER = "https://api.spotify.com";
    private static final String CONFIG_FILE = "application.properties";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE = "code";
    private static final String CLIENT_SECRET;
    private static final String REDIRECT_URI;
    private static final String CLIENT_ID;
    private static final Properties properties = new Properties();

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

    private String authorizationCode;
    private HttpRequest request;
    private HttpResponse<String> response;
    private String accessToken;
    private final HttpClient client = HttpClient.newBuilder().build();

    public boolean authorize() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println("https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=" + RESPONSE_TYPE);
        System.out.println("waiting for code...");

        requestAccessCode();
        if (authorizationCode == null) {
            return false;
        }
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        HttpResponse<String> response = requestAccessToken();
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        accessToken = json.get("access_token").getAsString();
        return response.statusCode() == 200;
    }

    private void requestAccessCode() throws IOException, InterruptedException {
        final var latch = new CountDownLatch(1);
        final var server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/",
                exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String responseBody;
                    if (query != null && query.contains("code")) {
                        authorizationCode = query.substring(5);
                        latch.countDown();
                        responseBody = "Got the code. Return back to your program.";
                    } else {
                        latch.countDown();
                        responseBody = "Not found authorization code. Try again.";
                    }
                    exchange.sendResponseHeaders(200, responseBody.length());
                    exchange.getResponseBody().write(responseBody.getBytes());
                    exchange.getResponseBody().close();
                }
        );

        server.start();
        latch.await();
        server.stop(10);
    }

    private HttpResponse<String> requestAccessToken() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=" + GRANT_TYPE
                                + "&code=" + authorizationCode
                                + "&redirect_uri=" + REDIRECT_URI
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
