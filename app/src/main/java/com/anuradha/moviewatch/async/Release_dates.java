package com.anuradha.moviewatch.async;

public class Release_dates
{
    private String iso_639_1;

    private String certification;

    private String release_date;

    private String type;

    public String getIso_639_1 ()
    {
        return iso_639_1;
    }

    public void setIso_639_1 (String iso_639_1)
    {
        this.iso_639_1 = iso_639_1;
    }

    public String getCertification ()
    {
        return certification;
    }

    public void setCertification (String certification)
    {
        this.certification = certification;
    }

    public String getRelease_date ()
    {
        return release_date;
    }

    public void setRelease_date (String release_date)
    {
        this.release_date = release_date;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [iso_639_1 = "+iso_639_1+", certification = "+certification+", release_date = "+release_date+", type = "+type+"]";
    }
}
