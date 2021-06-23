package advisor.service;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Auth {
    private String authorizationCode;

    public boolean getAccessCode() {
        String uri = Configuration.AUTH_SERVER + "/authorize"
                + "?client_id=" + Configuration.CLIENT_ID
                + "&redirect_uri=" + Configuration.REDIRECT_URI
                + "&response_type=code";
        System.out.println("use this link to request the access code:");
        System.out.println(uri);

        final var latch = new CountDownLatch(1);
        try {
            final var server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/",
                    exchange -> {
                        String query = exchange.getRequestURI().getQuery();
                        String responseBody;
                        if (query != null && query.contains("code")) {
                            authorizationCode = query.substring(5);
                            latch.countDown();
                            responseBody = "Got the code. Return back to your program.";
                        } else {
                            latch.countDown();
                            responseBody = "Not found authorization code. Try again.";
                        }
                        exchange.sendResponseHeaders(200, responseBody.length());
                        exchange.getResponseBody().write(responseBody.getBytes());
                        exchange.getResponseBody().close();
                    }
            );

            server.start();
            latch.await();
            server.stop(10);

        } catch (IOException | InterruptedException e) {
            return false;
        }
        return true;
    }
}
