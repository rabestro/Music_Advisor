package advisor.controller;

import advisor.model.Configuration;

public class NotAuthenticated implements Controller {
    private static final System.Logger LOGGER = System.getLogger("");

    private final Configuration config;

    public NotAuthenticated(Configuration configuration) {
        config = configuration;
    }

    public Controller authenticate() {
        config.setAccessToken("");
        return new Authenticated(config);
    }

    @Override
    public void process(String command) {
        System.out.println("Please, provide access for application.");
    }
}
