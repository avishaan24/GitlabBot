package com.microsoftTeams.bot;

import java.util.ArrayList;
import java.util.List;


/**
 * class which is used to store userEmail who want reviewList
 */
public class ReviewerUsers {
    private List<String> users;

    public ReviewerUsers() {
        this.users = new ArrayList<>();
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
