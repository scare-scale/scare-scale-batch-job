package com.scarescale.batch.movieupdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import info.movito.themoviedbapi.model.core.Movie;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBMovie {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final Integer id;
    private final String name;
    private final String tmdbBackdropId;
    private final LocalDate releaseDate;
    private final String synopsis;
    private final String tmdbPosterId;

    public DBMovie(final Movie movie) {
        this.id = movie.getId();
        this.name = movie.getTitle();
        this.tmdbBackdropId = movie.getBackdropPath();
        this.tmdbPosterId = movie.getPosterPath();
        this.releaseDate = LocalDate.parse(movie.getReleaseDate());
        this.synopsis = movie.getOverview();
    }

    @JsonProperty("id")
    public Integer getId() {
        return this.id;
    }

    @JsonProperty("tmdbBackdropId")
    public String getBackdropPath() {
        return this.tmdbBackdropId;
    }

    @JsonProperty("synopsis")
    public String getOverview() {
        return this.synopsis;
    }

    @JsonProperty("tmdbPosterId")
    public String getPosterPath() {
        return this.tmdbPosterId;
    }

    @JsonProperty("releaseDate")
    public String getReleaseDate() {
        return this.releaseDate.format(DATE_FORMATTER);
    }

    @JsonProperty("name")
    public String getTitle() {
        return this.name;
    }
}

