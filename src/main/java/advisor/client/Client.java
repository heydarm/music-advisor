package advisor.client;

import advisor.Config;
import advisor.data.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Client {
    private final HttpClient client;
    private final String accessServerPath;
    private final String resourceServerPath;

    public Client(String accessServerPath, String resourceServerPath) {
        this.accessServerPath = accessServerPath;
        this.resourceServerPath = resourceServerPath;
        client = HttpClient.newBuilder().build();
    }

    public void requestAccessToken(User user) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .uri(URI.create(accessServerPath + "/api/token"))
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code&" +
                            String.format("code=%s", user.getCode()) +
                            "&redirect_uri=" + Config.REDIRECT_URI +
                            "&client_id=" + Config.CLIENT_ID +
                            "&client_secret=" + Config.CLIENT_SECRET))
                    .build();

            HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
            JsonObject userAuthData = JsonParser.parseString(response.body()).getAsJsonObject();

            if (userAuthData != null) {
                user.setAuth(true);
                user.setAccessToken(userAuthData.get("access_token").getAsString());
                user.setTokenType(userAuthData.get("token_type").getAsString());
                user.setScope(userAuthData.get("scope").getAsString());
                user.setExpiresIn(userAuthData.get("expires_in").getAsInt());
                user.setRefreshToken(userAuthData.get("refresh_token").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject fetchFeatured(String token) {
        return fetchItemByName(token, "/v1/browse/featured-playlists", "playlists");
    }

    public JsonObject fetchNew(String token) {
        return fetchItemByName(token, "/v1/browse/new-releases", "albums");
    }

    public JsonObject fetchCategories(String token) {
        return fetchItemByName(token, "/v1/browse/categories", "categories");
    }

    public JsonObject fetchPlaylists(String token, String category) {
        return fetchItemByName(token, "/v1/browse/categories/" + category + "/playlists", "playlists");
    }

    private JsonObject fetchItemByName(String token, String path, String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .uri(URI.create(resourceServerPath + path))
                    .GET()
                    .build();

            HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
            JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();

            if (data != null) {
                return data.has("error") ? data.getAsJsonObject("error") : data.getAsJsonObject(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getUserAuthLink() {
        return accessServerPath +
                "/authorize?" +
                "client_id=" + Config.CLIENT_ID +
                "&redirect_uri=" + Config.REDIRECT_URI +
                "&response_type=code";
    }
}
