package advisor.server;

import advisor.data.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAuthHttpHandler implements HttpHandler {
    private final User user;
    private boolean requested = false;

    public UserAuthHttpHandler(User user) {
        this.user = user;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);

        Map<String, String> queriesMap = new HashMap<>();
        ArrayList<String> queries;

        if (exchange.getRequestURI().getQuery() != null) {
            queries = new ArrayList<>(List.of(exchange.getRequestURI().getQuery().split("&")));

            for (String query : queries) {
                queriesMap.put(query.split("=")[0], query.split("=")[1]);
            }

        }

        if (queriesMap.containsKey("code")) {
            user.setCode(queriesMap.get("code"));
            exchange.getResponseBody().write("Got the code. Return back to your program.".getBytes());
        } else {
            exchange.getResponseBody().write("Not found authorization code. Try again.".getBytes());
        }

        exchange.getResponseBody().close();
        requested = true;
    }

    public boolean isRequested() {
        return requested;
    }
}
