# PopularMovies
Searching for Movies? This Android app makes it easy to discover, search movies and mark them to your favorites. 
This Application opens a grid arrangement of the movieposters according to the selected sort-order (from Settings menu).
The order can be most-popular or highest-rated or favorites. When a poster is clicked the details of the coresponding movie is displayed. Each movies shows original title, movie poster, plot synopsis, user rating, release date, trailers and user reviews. It is compatible with all devices running on Ice Cream Sandwich and higher (API Level 15).

This app fetches data from themoviedb API, but is not endorsed or certified by TMDb. Favorited movies will be saved to the database (implemented using content provider), and will be viewable offline. However, As internet connectivity is required to establish connection to the movie database, the most popular and highest rated movies will not load without internet.
Features:
 *Movies search based on Popularity or Rating
 *Add/remove movie as favorite
 *Optimized app experience for tablet
 *Watch trailer on Youtube
 *Share trailers with friends
 *View user reviews
 *Image descriptions for accessibility readers
 *RTL support
Implements: _SyncAdapter; AsyncHander; SQLite database with custom Content Provider; CursorLoader; Preference Manager; Share Action Provider; FragmentManager; Intent; Shared Preferences; Activity lifecycle; HTTP requests, web APIs; Android Permissions; App navigation with Explicit Intents; GridView; ListView; Accessibility; Butterknife; Progress Wheel; Picasso ; Parcelable; Stetho._
This app was submitted as a project for Android Developer Nanodegree program on Udacity.

[IMPORTANT: for the URL to work, Please enter the APIkey in the gradle file in the app folder in the place of 
the string 'add api key within double quotes'.]
