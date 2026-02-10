package com.scarescale.batch.movieupdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import info.movito.themoviedbapi.model.core.Movie;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBMovie {

    @JsonIgnore
    private final Movie movie;

    public DBMovie(final Movie movie) {
        this.movie = movie;
    }

    @JsonProperty("id")
    public Integer getId() {
        return this.movie.getId();
    }

    @JsonProperty("tmdbBackdropId")
    public String getBackdropPath() {
        return this.movie.getBackdropPath();
    }

    @JsonProperty("synopsis")
    public String getOverview() {
        return this.movie.getOverview();
    }

    @JsonProperty("tmdbPosterId")
    public String getPosterPath() {
        return this.movie.getPosterPath();
    }

    @JsonProperty("releaseDate")
    public String getReleaseDate() {
        LocalDate date = LocalDate.parse(this.movie.getReleaseDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        return date.format(formatter);
    }

    @JsonProperty("name")
    public String getTitle() {
        return this.movie.getTitle();
    }
}
