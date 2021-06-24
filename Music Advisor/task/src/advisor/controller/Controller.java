package advisor.controller;

public interface Controller {
    Controller authenticate();

    void process(final String command);
}
