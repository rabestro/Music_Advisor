package advisor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import static java.lang.System.Logger.Level.*;

public class Authentication {
    private static final System.Logger LOGGER = System.getLogger("");

    private final Configuration config;
    private String authorizationCode;

    public Authentication(Configuration configuration) {
        this.config = configuration;
        LOGGER.log(INFO, "Authorization service created.");

    }

    public boolean getAccessCode() {
        String uri = config.getAuthLink();
        System.out.println("use this link to request the access code:");
        System.out.println(uri);

        final var latch = new CountDownLatch(1);
        try {
            final var server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);

            server.createContext("/",
                    exchange -> {
                        exchange.getRequestURI();
                        LOGGER.log(INFO, "getRequestURI {0}", exchange.getRequestURI());
                        String query = exchange.getRequestURI().getQuery();
                        String responseBody;
                        if (query != null && query.contains("code")) {
                            authorizationCode = query.substring(5);
                            latch.countDown();
                            System.out.println("code received");
                            System.out.println(authorizationCode);
                            responseBody = "Got the code. Return back to your program.";
                            LOGGER.log(INFO, "Authorization Code: {0}", authorizationCode);
                        } else {
                            latch.countDown();
                            responseBody = "Not found authorization code. Try again.";
                            LOGGER.log(ERROR, "Not found authorization code.");
                        }
                        exchange.sendResponseHeaders(200, responseBody.length());
                        exchange.getResponseBody().write(responseBody.getBytes());
                        exchange.getResponseBody().close();
                    }
            );

            server.start();
            latch.await();
            server.stop(10);

        } catch (InterruptedException | IOException e) {
            return false;
        }
        return true;
    }
}
