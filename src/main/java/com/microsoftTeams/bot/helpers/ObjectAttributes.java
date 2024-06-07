package com.microsoftTeams.bot.helpers;

/**
 * ObjectAttributes which is used to store details about the webhook notification from Gitlab
 */

public class ObjectAttributes {
    private Long author_id;
    private Long id;
    private String state;
    private String title;
    private LastCommit last_commit;
    private String note;
    private String noteable_type;
    private Long noteable_id;
    private String url;
    private String status;
    private String ref;

    public ObjectAttributes() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(Long author_id) {
        this.author_id = author_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNoteable_type() {
        return noteable_type;
    }

    public void setNoteable_type(String noteable_type) {
        this.noteable_type = noteable_type;
    }

    public LastCommit getLast_commit() {
        return last_commit;
    }

    public void setLast_commit(LastCommit last_commit) {
        this.last_commit = last_commit;
    }

    public Long getNoteable_id() {
        return noteable_id;
    }

    public void setNoteable_id(Long noteable_id) {
        this.noteable_id = noteable_id;
    }
}
