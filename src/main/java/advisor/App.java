package advisor;

import advisor.client.Client;
import advisor.data.Database;
import advisor.data.Playlist;
import advisor.server.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class App {
    private final Database db;
    private final View view;
    private final Server server;
    private final Client client;
    private State state;
    private JsonArray currentData;

    public App(Database db, String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-access":
                    Config.ACCESS_SERVER_PATH = args[i + 1];
                    break;
                case "-resource":
                    Config.RESOURCE_SERVER_PATH = args[i + 1];
                    break;
                case "-page":
                    Config.ITEMS_PER_PAGE = Integer.parseInt(args[i + 1]);
                    break;
            }
        }

        this.db = db;
        view = new View(this);
        server = new Server();
        client = new Client(Config.ACCESS_SERVER_PATH, Config.RESOURCE_SERVER_PATH);
    }

    public Server getServer() {
        return server;
    }

    public Client getClient() {
        return client;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean auth = false;
            boolean stop = false;

            while (scanner.hasNextLine() && !stop) {
                String option = scanner.nextLine();

                if ("auth".equals(option)) {
                    view.displayAuthProcess(db.getUser());
                    if (db.getUser().isAuth()) {
                        auth = true;
                    }
                } else if ("exit".equals(option)) {
                    return;
                } else if (auth) {
                    updateCategoriesMap(db.getUser().getAccessToken());
                    inputOptionsHandler(option);
                } else {
                    view.display("Please, provide access for application.");
                }
            }
        }
    }

    private void inputOptionsHandler(String option) {
        String accessToken = db.getUser().getAccessToken();

        if (option.contains("playlists")) {
            String[] playlist = option.split("playlists ");

            if (playlist.length > 1) {
                displayNextPage(handlePlaylists(accessToken, playlist[1]));
            } else {
                view.display("You did't enter playlist name. Please try again.");
            }

            return;
        }

        switch (option) {
            case "featured":
                displayNextPage(handleFeatured(accessToken));
                break;
            case "new":
                displayNextPage(handleNew(accessToken));
                break;
            case "categories":
                displayNextPage(handleCategories(accessToken));
                break;
            case "next":
                if (state != null && currentData != null) {
                    displayNextPage(currentData);
                } else {
                    System.out.println("Please choose the option");
                }
                break;
            case "prev":
                if (state != null && currentData != null) {
                    displayPrevPage(currentData);
                } else {
                    System.out.println("Please choose the option");
                }
                break;
            default:
                view.display("There is no such such option. Available options: " +
                        "auth, featured, new, categories, playlists 'CATEGORY_NAME', exit");
        }
    }

    public JsonArray handleFeatured(String token) {
        return handleFetchData(client.fetchFeatured(token), State.FEATURED);
    }

    public JsonArray handleNew(String token) {
        return handleFetchData(client.fetchNew(token), State.NEW);
    }

    public JsonArray handleCategories(String token) {
        return handleFetchData(client.fetchCategories(token), State.CATEGORIES);
    }

    public JsonArray handlePlaylists(String token, String category) {
        return handleFetchData(client.fetchPlaylists(token, db.getPlaylistIdByName(category)), State.PLAYLISTS);
    }

    private JsonArray handleFetchData(JsonObject jsonObject, State state) {
        state.setCurrPage(0);
        currentData = null;

        if (checkDataCorrectness(jsonObject)) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("items");
            currentData = jsonArray;
            this.state = state;

            return jsonArray;
        }

        return null;
    }

    private void updateCategoriesMap(String token) {
        JsonObject fetchedData = client.fetchCategories(token);

        if (checkDataCorrectness(fetchedData)) {
            JsonArray categories = fetchedData.getAsJsonArray("items");

            for (int i = 0; i < categories.size(); i++) {
                JsonObject category = categories.get(i).getAsJsonObject();

                db.addPlaylist(new Playlist(getNameFromJsonObject(category),
                        category.get("id").toString().replaceAll("\"", "")));
            }
        }
    }

    private boolean checkDataCorrectness(JsonObject data) {
        if (data != null) {
            if (data.has("status") && data.has("message")) {
                view.display(data.get("message").toString());
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private String getNameFromJsonObject(JsonObject data) {
        return data.get("name").toString().replaceAll("\"", "");
    }

    private String getUrlFromJsonObject(JsonObject data) {
        return data.get("external_urls").getAsJsonObject()
                .get("spotify").toString().replaceAll("\"", "");
    }

    private String formArtists(JsonObject jsonObject) {
        JsonArray artists = jsonObject.get("artists").getAsJsonArray();

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int j = 0; j < artists.size(); j++) {
            sb.append(getNameFromJsonObject(artists.get(j).getAsJsonObject()));
            sb.append((j == artists.size() - 1) ? "" : ", ");
        }

        sb.append("]");

        return sb.toString();
    }

    private void displayNextPage(JsonArray jsonArray) {
        displayPage(Page.NEXT, jsonArray);
    }

    private void displayPrevPage(JsonArray jsonArray) {
        displayPage(Page.PREV, jsonArray);
    }

    private void displayPage(Page page, JsonArray jsonArray) {
        if (jsonArray != null) {
            state.setMaxPage((int) Math.ceil((double) jsonArray.size() / Config.ITEMS_PER_PAGE));

            if (page == Page.NEXT) {
                state.incrementCurrPage();
            } else {
                state.decrementCurrPage();
            }

            if (state.getCurrPage() <= state.getMaxPage() && state.getCurrPage() > 0) {
                for (int i = (state.getCurrPage() - 1) * Config.ITEMS_PER_PAGE;
                     i < state.getCurrPage() * Config.ITEMS_PER_PAGE && i < jsonArray.size();
                     i++) {
                    JsonObject item = jsonArray.get(i).getAsJsonObject();

                    if (state == State.CATEGORIES) {
                        view.displayOptionResult(getNameFromJsonObject(item));
                    } else if (state == State.FEATURED || state == State.PLAYLISTS) {
                        view.displayOptionResult(getNameFromJsonObject(item), getUrlFromJsonObject(item));
                    } else {
                        view.displayOptionResult(getNameFromJsonObject(item), formArtists(item), getUrlFromJsonObject(item));
                    }
                }

                view.display(String.format("---PAGE %s OF %s---", state.getCurrPage(), state.getMaxPage()));
            } else {
                if (page == Page.NEXT) {
                    state.decrementCurrPage();
                } else {
                    state.incrementCurrPage();
                }
                view.display("No more pages.");
            }
        }
    }

    private enum Page {
        NEXT,
        PREV
    }
}
