package advisor;

public enum State {
    PLAYLISTS,
    FEATURED,
    NEW,
    CATEGORIES;

    private int currPage = 0;
    private int maxPage = 0;

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public void incrementCurrPage() {
        this.currPage++;
    }

    public void decrementCurrPage() {
        this.currPage--;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }
}
