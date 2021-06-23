package advisor;

import advisor.service.Configuration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.net.URI;

public class Controller {
    private static final System.Logger LOGGER = System.getLogger("");

    private String authorizationCode;
    private HttpRequest request;
    private HttpResponse<String> response;
    private String accessToken;
    private final HttpClient client = HttpClient.newBuilder().build();

    public boolean authorize() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println("https://accounts.spotify.com/authorize"
                + "?client_id=" + Configuration.CLIENT_ID
                + "&redirect_uri=" + Configuration.REDIRECT_URI
                + "&response_type=" + Configuration.RESPONSE_TYPE);
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
                        "grant_type=" + Configuration.GRANT_TYPE
                                + "&code=" + authorizationCode
                                + "&redirect_uri=" + Configuration.REDIRECT_URI
                                + "&client_id=" + Configuration.CLIENT_ID
                                + "&client_secret=" + Configuration.CLIENT_SECRET))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
