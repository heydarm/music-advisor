## Music Advisor App  
A CLI based app used to receive interesting playlists, songs, etc. from Spotify.  

#### Using:  
Fist you need to log in using your Spotify account.
As this app is CLI based you should interact with it by entering available keywords:  
 - `auth` - prints authorization link. By following it you will be redirected to Spotify,
 where you should authorize through your account and give access;
 - `featured` - get a list of featured playlists;
 - `new` - get a list of new releases;
 - `categories` - get a list of categories;
 - `playlists {category_name}` - get a category's playlists ({category_name} - name of available category) ;
 - `exit` - stops the application.
 
Additional options (allowed after entering featured, new, categories, playlists):
 - `next` - next page;
 - `prev` - previous page.
 
#### Configuration:
Configuration is situated in "Config.java" file.
1. `ACCESS_SERVER_PATH` - path to Spotify authorization server;
1. `RESOURCE_SERVER_PATH` - path to Spotify resource (playlists, songs, etc.) server;
1. `ITEMS_PER_PAGE` - how many item will be displayed per page;
1. `REDIRECT_URI` - you should enter the same url which you used in your.