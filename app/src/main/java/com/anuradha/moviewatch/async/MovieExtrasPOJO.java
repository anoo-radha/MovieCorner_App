package com.anuradha.moviewatch.async;

public class MovieExtrasPOJO
{
    public class ReleaseDates
    {
        private Results[] results;

        public Results[] getResults() { return this.results; }

        public void setResults(Results[] results) { this.results = results; }
    }

    private String budget;

    private Genres[] genres;

    private String vote_average;

    private Credits credits;

    private String runtime;

//    private Spoken_languages[] spoken_languages;

    private ReleaseDates release_dates;

    private String id;

    private String title;

    private String original_title;

    private String video;

    private String popularity;

    private String revenue;

    private String backdrop_path;

    private String status;

    private String adult;

    private String homepage;

//    private Production_countries[] production_countries;

    private String overview;

    private String original_language;

//    private Production_companies[] production_companies;

    private String imdb_id;

//    private null belongs_to_collection;

    private String release_date;

    private String vote_count;

    private String poster_path;

    private String tagline;

    public String getBudget ()
    {
        return budget;
    }

    public void setBudget (String budget)
    {
        this.budget = budget;
    }

    public Genres[] getGenres ()
    {
        return genres;
    }

    public void setGenres (Genres[] genres)
    {
        this.genres = genres;
    }

    public String getVote_average ()
    {
        return vote_average;
    }

    public void setVote_average (String vote_average)
    {
        this.vote_average = vote_average;
    }

    public Credits getCredits ()
    {
        return credits;
    }

    public void setCredits (Credits credits)
    {
        this.credits = credits;
    }

    public String getRuntime ()
    {
        return runtime;
    }

    public void setRuntime (String runtime)
    {
        this.runtime = runtime;
    }

//    public Spoken_languages[] getSpoken_languages ()
//    {
//        return spoken_languages;
//    }
//
//    public void setSpoken_languages (Spoken_languages[] spoken_languages)
//    {
//        this.spoken_languages = spoken_languages;
//    }

    public ReleaseDates getRelease_dates ()
    {
        return release_dates;
    }

    public void setRelease_dates (ReleaseDates release_dates)
    {
        this.release_dates = release_dates;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getOriginal_title ()
    {
        return original_title;
    }

    public void setOriginal_title (String original_title)
    {
        this.original_title = original_title;
    }

    public String getVideo ()
    {
        return video;
    }

    public void setVideo (String video)
    {
        this.video = video;
    }

    public String getPopularity ()
    {
        return popularity;
    }

    public void setPopularity (String popularity)
    {
        this.popularity = popularity;
    }

    public String getRevenue ()
    {
        return revenue;
    }

    public void setRevenue (String revenue)
    {
        this.revenue = revenue;
    }

    public String getBackdrop_path ()
    {
        return backdrop_path;
    }

    public void setBackdrop_path (String backdrop_path)
    {
        this.backdrop_path = backdrop_path;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getAdult ()
    {
        return adult;
    }

    public void setAdult (String adult)
    {
        this.adult = adult;
    }

    public String getHomepage ()
    {
        return homepage;
    }

    public void setHomepage (String homepage)
    {
        this.homepage = homepage;
    }

//    public Production_countries[] getProduction_countries ()
//    {
//        return production_countries;
//    }
//
//    public void setProduction_countries (Production_countries[] production_countries)
//    {
//        this.production_countries = production_countries;
//    }

    public String getOverview ()
    {
        return overview;
    }

    public void setOverview (String overview)
    {
        this.overview = overview;
    }

    public String getOriginal_language ()
    {
        return original_language;
    }

    public void setOriginal_language (String original_language)
    {
        this.original_language = original_language;
    }

//    public Production_companies[] getProduction_companies ()
//    {
//        return production_companies;
//    }
//
//    public void setProduction_companies (Production_companies[] production_companies)
//    {
//        this.production_companies = production_companies;
//    }

    public String getImdb_id ()
    {
        return imdb_id;
    }

    public void setImdb_id (String imdb_id)
    {
        this.imdb_id = imdb_id;
    }

//    public null getBelongs_to_collection ()
//{
//    return belongs_to_collection;
//}
//
//    public void setBelongs_to_collection (null belongs_to_collection)
//    {
//        this.belongs_to_collection = belongs_to_collection;
//    }

    public String getRelease_date ()
    {
        return release_date;
    }

    public void setRelease_date (String release_date)
    {
        this.release_date = release_date;
    }

    public String getVote_count ()
    {
        return vote_count;
    }

    public void setVote_count (String vote_count)
    {
        this.vote_count = vote_count;
    }

    public String getPoster_path ()
    {
        return poster_path;
    }

    public void setPoster_path (String poster_path)
    {
        this.poster_path = poster_path;
    }

    public String getTagline ()
    {
        return tagline;
    }

    public void setTagline (String tagline)
    {
        this.tagline = tagline;
    }

//    @Override
//    public String toString()
//    {
//        return "ClassPojo [budget = "+budget+", genres = "+genres+", vote_average = "+vote_average+", credits = "+credits+", runtime = "+runtime+", spoken_languages = "+spoken_languages+", release_dates = "+release_dates+", id = "+id+", title = "+title+", original_title = "+original_title+", video = "+video+", popularity = "+popularity+", revenue = "+revenue+", backdrop_path = "+backdrop_path+", status = "+status+", adult = "+adult+", homepage = "+homepage+", production_countries = "+production_countries+", overview = "+overview+", original_language = "+original_language+", production_companies = "+production_companies+", imdb_id = "+imdb_id+", belongs_to_collection = "+belongs_to_collection+", release_date = "+release_date+", vote_count = "+vote_count+", poster_path = "+poster_path+", tagline = "+tagline+"]";
//    }
}