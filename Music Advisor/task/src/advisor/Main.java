package advisor;

import advisor.model.Configuration;
import advisor.view.Application;

public class Main {
    public static void main(String[] args) {
        new Application(new Configuration(args)).run();
    }
}
