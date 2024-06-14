package com.microsoftTeams.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoftTeams.bot.helpers.*;

import java.util.List;

/**
 * Information class stores event information of webhooks from the Gitlab
 */

public class Information {

    @JsonProperty("object_kind")
    private String objectKind;

    @JsonProperty("object_attributes")
    private ObjectAttributes objectAttributes;

    @JsonProperty("user")
    private User user;

    @JsonProperty("project")
    private Project project;

    @JsonProperty("merge_request")
    private MergeRequest mergeRequest;

    @JsonProperty("commit")
    private Commits commit;

    @JsonProperty("builds")
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

    public MergeRequest getMergeRequest() {
        return mergeRequest;
    }

    public void setMergeRequest(MergeRequest mergeRequest) {
        this.mergeRequest = mergeRequest;
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

    public ObjectAttributes getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(ObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }

    public String getObjectKind() {
        return objectKind;
    }

    public void setObjectKind(String objectKind) {
        this.objectKind = objectKind;
    }
}
