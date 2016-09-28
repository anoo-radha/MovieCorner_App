package com.anuradha.moviewatch.async;

public class Credits
{
    private Cast[] cast;

    private Crew[] crew;

    public Cast[] getCast ()
    {
        return cast;
    }

    public void setCast (Cast[] cast)
    {
        this.cast = cast;
    }

    public Crew[] getCrew ()
    {
        return crew;
    }

    public void setCrew (Crew[] crew)
    {
        this.crew = crew;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [cast = "+cast+", crew = "+crew+"]";
    }
}
