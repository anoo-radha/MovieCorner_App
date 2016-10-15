package com.anuradha.moviewatch.async;

public class Results
{
    private Release_dates[] release_dates;

    private String iso_3166_1;

    public Release_dates[] getRelease_dates ()
    {
        return release_dates;
    }

    public void setRelease_dates (Release_dates[] release_dates)
    {
        this.release_dates = release_dates;
    }

    public String getIso_3166_1 ()
    {
        return iso_3166_1;
    }

    public void setIso_3166_1 (String iso_3166_1)
    {
        this.iso_3166_1 = iso_3166_1;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [release_dates = "+release_dates+", iso_3166_1 = "+iso_3166_1+"]";
    }
}