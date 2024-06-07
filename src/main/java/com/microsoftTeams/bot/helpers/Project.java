package com.microsoftTeams.bot.helpers;

/**
 * Project class which is used to store project details from which the event belongs
 */

public class Project {
    private String name;
    private String web_url;

    public Project() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeb_url() {
        return web_url;
    }

    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }
}
