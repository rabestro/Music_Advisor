package advisor.controller;

import advisor.model.Configuration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public void getAccessCode() throws IOException, InterruptedException {
        final var executorService = Executors.newSingleThreadExecutor();
        final var countDownLatch = new CountDownLatch(1);
        final var server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);

        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String request;
            if (query != null && query.contains("code")) {
                code = query.substring(5);
                request = "Got the code. Return back to your program.";
                countDownLatch.countDown();
                LOGGER.log(INFO, "Authorization Code: {0}", code);
            } else {
                request = "Authorization code not found. Try again.";
                LOGGER.log(WARNING, "Not found authorization code.");
            }
            exchange.sendResponseHeaders(200, request.length());
            exchange.getResponseBody().write(request.getBytes());
            exchange.getResponseBody().close();
        });

        server.setExecutor(executorService);      // set up a custom executor for the server
        server.start();              // start the server
        System.out.println("waiting for code...");
        countDownLatch.await();                   // wait until `c.countDown()` is invoked
        executorService.shutdown();               // send shutdown command to executor
        // wait until all tasks complete (i. e. all responses are sent)
        executorService.awaitTermination(1, TimeUnit.HOURS);
        server.stop(5);
    }

    public boolean getAuthentication() {
        try {
            getAccessCode();
            getAccessToken();
            return true;
        } catch (InterruptedException | IOException e) {
            LOGGER.log(ERROR, e::getMessage);
            return false;
        }
    }

    public String getAccessToken() throws IOException, InterruptedException {
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

        final var client = HttpClient.newBuilder().build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        String accessToken = jo.get("access_token").getAsString();
        LOGGER.log(DEBUG, response::body);
        System.out.println(response.body());
        return accessToken;
    }

}
