package com.microsoftTeams.bot.helpers;

/**
 * Author class which is used to store author details who commit anything in the repo
 */

public class Author {
    private String name;
    private String email;

    public Author() {
    }

    public Author(Author author){
        this.name = author.getName();
        this.email = author.getEmail();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
