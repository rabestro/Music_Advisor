package advisor;

import advisor.service.Configuration;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.IOException;

public class Authorization {


    private static final String REDIRECT_URI = "http://localhost:8080";
    static String AUTH_SERVER = "https://accounts.spotify.com";
    static String API_SERVER = "https://api.spotify.com";
    static String ACCESS_CODE = "";
    static String ACCESS_TOKEN= "";
    static boolean isAuthorized = false;

    static void setAuth() {
        Authorization authorization = new Authorization();
        authorization.getAccessCode();
        authorization.getAccessToken();
        isAuthorized = true;
    }

    void getAccessCode() {
        String uri = AUTH_SERVER + "/authorize"
                + "?client_id=" + Configuration.CLIENT_ID
                + "&redirect_uri=" + Configuration.REDIRECT_URI
                + "&response_type=code";
        System.out.println("use this link to request the access code:");
        System.out.println(uri);
        try {
            /* Creating a server */
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.start();
            server.createContext("/",
                    exchange -> {
                        var query = exchange.getRequestURI().getQuery();
                        final String request;
                        if (query != null && query.contains("code")) {
                            ACCESS_CODE = query.substring(5);
                            System.out.println("code received");
                            System.out.println(ACCESS_CODE);
                            request = "Got the code. Return back to your program.";
                        } else {
                            request = "Authorization code not found. Try again.";
                        }
                        exchange.sendResponseHeaders(200, request.length());
                        exchange.getResponseBody().write(request.getBytes());
                        exchange.getResponseBody().close();
                    });
            System.out.println("waiting for code...");
            while (ACCESS_CODE.length() == 0) {
                Thread.sleep(100);
            }
            server.stop(5);
        } catch (IOException | InterruptedException e) {
            System.out.println("Server error.");
        }
    }

    void getAccessToken() {
        System.out.println("making http request for access_token...");
        System.out.println("response:");
        /* Getting access token based on access code */
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(AUTH_SERVER + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code"
                        + "&code=" + ACCESS_CODE
                        + "&client_id=" + Configuration.CLIENT_ID
                        + "&client_secret=" + Configuration.CLIENT_SECRET
                        + "&redirect_uri=" + REDIRECT_URI))
                .build();
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assert response != null;
            System.out.println(response.body());
            System.out.println("---SUCCESS---");
            /* Parsing access token from response */
            ACCESS_TOKEN = JsonParser.parseString(response.body()).getAsJsonObject().get("access_token").getAsString();
        } catch (InterruptedException | IOException e) {
            System.out.println("Error response.");
        }
    }
}