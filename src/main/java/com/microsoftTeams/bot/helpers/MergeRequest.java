package com.microsoftTeams.bot.helpers;

/**
 * MergeRequest class which is used to store data of the event type merge_request in webhooks Gitlab
 */

public class MergeRequest {
    private Long id;
    private String title;
    private LastCommit last_commit;
    private String state;

    public MergeRequest() {
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LastCommit getLast_commit() {
        return last_commit;
    }

    public void setLast_commit(LastCommit last_commit) {
        this.last_commit = last_commit;
    }
}
