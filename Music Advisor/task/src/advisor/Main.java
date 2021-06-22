package advisor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    static boolean auth = false;
    static Controller controller = new Controller();

    private static final String URL = "https://accounts.spotify.com/authorize?"
            + "client_id=81bcb1ba1c224b74b5f2bed6bb185cad"
            + "&redirect_uri=http://localhost:8080&response_type=code";

    public static void main(String[] args) throws IOException, InterruptedException {
        final var scanner = new Scanner(System.in);
        for (; ; ) {
            final var command = scanner.nextLine();
            switch (command) {
                case "auth":
                    auth = controller.authorize();
                    System.out.println("---SUCCESS---");
                    break;
                case "new":
                    print("---NEW RELEASES---\n" +
                            "Mountains [Sia, Diplo, Labrinth]\n" +
                            "Runaway [Lil Peep]\n" +
                            "The Greatest Show [Panic! At The Disco]\n" +
                            "All Out Life [Slipknot]");
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

    static void print(String message) {
        System.out.println(auth ? message : "Please, provide access for application.");
    }
}
