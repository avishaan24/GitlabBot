package com.microsoftTeams.bot;

import com.microsoftTeams.bot.helpers.*;

import java.util.List;

/**
 * Information class stores event information of webhooks from the Gitlab
 */

public class Information {
    private String object_kind;
    private ObjectAttributes object_attributes;
    private User user;
    private Project project;
    private MergeRequest merge_request;
    private Commits commit;
    private List<Builds> builds;

    public Information() {
    }

    public Commits getCommit() {
        return commit;
    }

    public void setCommit(Commits commit) {
        this.commit = commit;
    }

    public List<Builds> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Builds> builds) {
        this.builds = builds;
    }

    public MergeRequest getMerge_request() {
        return merge_request;
    }

    public void setMerge_request(MergeRequest merge_request) {
        this.merge_request = merge_request;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ObjectAttributes getObject_attributes() {
        return object_attributes;
    }

    public void setObject_attributes(ObjectAttributes object_attributes) {
        this.object_attributes = object_attributes;
    }

    public String getObject_kind() {
        return object_kind;
    }

    public void setObject_kind(String object_kind) {
        this.object_kind = object_kind;
    }
}
