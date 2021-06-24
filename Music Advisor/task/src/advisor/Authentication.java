package advisor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.System.Logger.Level.*;

public class Authentication {
    private static final System.Logger LOGGER = System.getLogger("");

    private final Configuration config;
    private String code;

    public Authentication(Configuration configuration) {
        this.config = configuration;
        LOGGER.log(INFO, "Authorization service created.");
    }

    /**
     * Getting access_code
     */
    public boolean getAccessCode() {
        System.out.println("use this link to request the access code:");
        System.out.println(config.getAuthLink());

        //Creating a server and listening to the request.
        try {
            final var server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);
            server.createContext("/", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String request;
                if (query != null && query.contains("code")) {
                    code = query.substring(5);
                    request = "Got the code. Return back to your program.";
                    LOGGER.log(INFO, "Authorization Code: {0}", code);
                } else {
                    request = "Authorization code not found. Try again.";
                    LOGGER.log(WARNING, "Not found authorization code.");
                }
                exchange.sendResponseHeaders(200, request.length());
                exchange.getResponseBody().write(request.getBytes());
                exchange.getResponseBody().close();
            });

            server.start();
            System.out.println("waiting for code...");
            while (code == null || code.length() == 0) {
                Thread.sleep(1000);
            }
            server.stop(5);

        } catch (IOException | InterruptedException e) {
            LOGGER.log(ERROR, e::getMessage);
            return false;
        }
        return true;
    }

    public boolean getAuthentication() {
        return getAccessCode() && getAccessToken();
    }

    public boolean getAccessToken() {
        LOGGER.log(INFO, "Request access_token...");

        final var request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(config.getAuthServer() + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code"
                                + "&code=" + code
                                + "&client_id=" + config.getClientId()
                                + "&client_secret=" + config.getClientSecret()
                                + "&redirect_uri=" + config.getRedirectUri()))
                .build();

        try {

            final var client = HttpClient.newBuilder().build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.log(DEBUG, response::body);
            System.out.println(response.body());
        } catch (InterruptedException | IOException e) {
            LOGGER.log(ERROR, e::getMessage);
            return false;
        }
        return true;
    }

}
