package advisor;

import advisor.data.User;

public class View {
    private final App app;

    public View(App app) {
        this.app = app;
    }

    public void display(String msg) {
        System.out.println(msg);
    }

    public void displayOptionResult(String... params) {
        switch (params.length) {
            case 1:
                System.out.println(params[0]);
                break;
            case 2:
                System.out.println(params[0]);
                System.out.println(params[1]);
                System.out.println();
                break;
            case 3:
                System.out.println(params[0]);
                System.out.println(params[1]);
                System.out.println(params[2]);
                System.out.println();
                break;
        }
    }

    public void displayAuthProcess(User user) {
        if (user.isAuth()) {
            System.out.println("You're already authorized.");
        } else {
            System.out.println("use this link to request the access code:");
            System.out.println(app.getClient().getUserAuthLink());
            System.out.println("waiting for code...");

            app.getServer().requestAuthCode(user);

            if (user.getCode() != null) {
                System.out.println("code received");
            } else {
                System.out.println("Error. Cannot receive authorization code. Please try again.");
                return;
            }

            System.out.println("Making http request for access_token...");

            app.getClient().requestAccessToken(user);

            if (user.getAccessToken() != null) {
                System.out.println(user.getAccessToken());
                System.out.println("Success!");
            } else {
                System.out.println("Error. Cannot receive access token. Please try again.");
            }
        }
    }
}
