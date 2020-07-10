package advisor.data;

import java.util.ArrayList;

public class Database {
    private final User user;
    private final ArrayList<Playlist> playlists;

    public Database() {
        user = new User();
        playlists = new ArrayList<>();
    }

    public User getUser() {
        return user;
    }

    public String getPlaylistIdByName(String name) {
        for (Playlist playlist : playlists) {
            if (name.equals(playlist.getName())) {
                return playlist.getId();
            }
        }

        return null;
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }
}
