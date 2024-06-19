package com.microsoftTeams.bot.helpers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Author class which is used to store author details who commit anything in the repo
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
