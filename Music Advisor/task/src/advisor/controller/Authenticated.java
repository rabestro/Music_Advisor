package advisor.controller;

import advisor.model.Configuration;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Authenticated implements Controller {
    private final Configuration config;

    public Authenticated(final Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public Controller authenticate() {
        return this;
    }

    @Override
    public void process(String command) {
        switch (command) {
            case "new":
                try {
                    newReleases();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }


    private void newReleases() throws IOException, InterruptedException {
        var apiPath = "https://api.spotify.com/v1/browse/new-releases";
        var request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + config.getAccessToken())
                .uri(URI.create(apiPath))
                .GET()
                .build();
        final var client = HttpClient.newBuilder().build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        final var jo = JsonParser.parseString(response.body()).getAsJsonObject();

    }
}
