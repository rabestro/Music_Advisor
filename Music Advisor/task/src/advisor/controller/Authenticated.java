package advisor.controller;

import advisor.model.Configuration;

import java.net.URI;
import java.net.http.HttpRequest;

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
                var apiPath = "https://api.spotify.com/v1/browse/new-releases";
                var httpRequest = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + config.getAccessToken())
                    .uri(URI.create(apiPath))
                    .GET()
                    .build();
        }
    }


}
