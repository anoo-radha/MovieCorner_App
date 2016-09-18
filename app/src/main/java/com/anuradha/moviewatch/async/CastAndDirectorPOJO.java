package com.anuradha.moviewatch.async;

public class CastAndDirectorPOJO {
    private String id;

    private Cast[] cast;

    private Crew[] crew;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cast[] getCast() {
        return cast;
    }

    public void setCast(Cast[] cast) {
        this.cast = cast;
    }

    public Crew[] getCrew() {
        return crew;
    }

    public void setCrew(Crew[] crew) {
        this.crew = crew;
    }

//    @Override
//    public String toString() {
//        return "ClassPojo [id = " + id + ", cast = " + cast + ", crew = " + crew + "]";
//    }
}
