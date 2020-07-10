package advisor.server;

import advisor.data.User;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Server {
    private HttpServer server;
    private UserAuthHttpHandler userAuth;

    public void requestAuthCode(User user) {
        try {
            userAuth = new UserAuthHttpHandler(user);
            runServer();
            server.createContext("/", userAuth);
            waitForCode();
            server.stop(1);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.start();
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void waitForCode() {
        Thread wait = new Thread(() -> {
            while (!userAuth.isRequested()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        wait.start();

        try {
            wait.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
