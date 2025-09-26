package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private String link;
    private String versions_micro;
    private String versions_small;
    private String versions_medium;
    private String versions_large;

    // Getters et Setters
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getVersions_micro() {
        return versions_micro;
    }

    public void setVersions_micro(String versions_micro) {
        this.versions_micro = versions_micro;
    }

    public String getVersions_small() {
        return versions_small;
    }

    public void setVersions_small(String versions_small) {
        this.versions_small = versions_small;
    }

    public String getVersions_medium() {
        return versions_medium;
    }

    public void setVersions_medium(String versions_medium) {
        this.versions_medium = versions_medium;
    }

    public String getVersions_large() {
        return versions_large;
    }

    public void setVersions_large(String versions_large) {
        this.versions_large = versions_large;
    }
}