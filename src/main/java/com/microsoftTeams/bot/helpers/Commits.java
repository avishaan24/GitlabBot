package com.microsoftTeams.bot.helpers;

/**
 * Commits class which store data related to the commit in the repo
 */

public class Commits {
    private String id;
    private String url;
    private Author author;

    public Commits() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
