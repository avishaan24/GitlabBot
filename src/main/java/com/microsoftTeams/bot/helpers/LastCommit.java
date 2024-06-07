package com.microsoftTeams.bot.helpers;

/**
 * LastCommit class which store the information about the lastCommit in the repo or in the open PR
 */

public class LastCommit {
    private String message;
    private String title;
    private Author author;

    public LastCommit() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
