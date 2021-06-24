package advisor.view;

import advisor.controller.Authentication;
import advisor.controller.Controller;
import advisor.controller.NotAuthenticated;
import advisor.model.Configuration;

import java.util.Scanner;

import static java.lang.System.Logger.Level.INFO;

public class Application implements Runnable {
    private static final System.Logger LOGGER = System.getLogger("");
    private static final Scanner scanner = new Scanner(System.in);

    private final Configuration config;
    private Controller controller;

    private boolean auth = false;

    public Application(Configuration configuration) {
        this.config = configuration;
        controller = new NotAuthenticated(configuration);
        LOGGER.log(INFO, "Application started.");
    }

    @Override
    public void run() {
        for (; ; ) {
            final var command = scanner.nextLine();
            switch (command) {
                case "auth":
                    System.out.println("use this link to request the access code:");
                    System.out.println(config.getAuthLink());
                    controller = controller.authenticate();
                    System.out.println("---SUCCESS---");
                    break;
                case "new":
                    controller.process("new");
//                    print("---NEW RELEASES---\n" +
//                            "Mountains [Sia, Diplo, Labrinth]\n" +
//                            "Runaway [Lil Peep]\n" +
//                            "The Greatest Show [Panic! At The Disco]\n" +
//                            "All Out Life [Slipknot]");
                    break;
                case "featured":
                    print("---FEATURED---\n" +
                            "Mellow Morning\n" +
                            "Wake Up and Smell the Coffee\n" +
                            "Monday Motivation\n" +
                            "Songs to Sing in the Shower");
                    continue;
                case "categories":
                    print("---CATEGORIES---\n" +
                            "Top Lists\n" +
                            "Pop\n" +
                            "Mood\n" +
                            "Latin");
                    continue;
                case "playlists Mood":
                    print("---MOOD PLAYLISTS---\n" +
                            "Walk Like A Badass  \n" +
                            "Rage Beats  \n" +
                            "Arab Mood Booster  \n" +
                            "Sunday Stroll");
                    continue;
                case "exit":
                    System.out.println("---GOODBYE!---");
                    return;
            }
        }
    }

    private void print(String message) {
        System.out.println(auth ? message : "Please, provide access for application.");
    }

}
