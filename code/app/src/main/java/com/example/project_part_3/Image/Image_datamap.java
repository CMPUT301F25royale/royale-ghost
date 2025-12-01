package com.example.project_part_3.Image;


import java.util.UUID;

public class Image_datamap {
    private String path;
    private String id;
    private String url;
    private String type;

    private String description;
    private String owner;
    private String associated_user;

    public Image_datamap() {
    }

    public Image_datamap(String id, String url, String path, String type, String description, String owner, String associated_user) {
        this.id = id;
        this.url = url;
        this.path = path;
        this.type = type;
        this.description = description;
        this.owner = owner;
        this.associated_user = associated_user;
    }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getOwner() { return owner; }
    public void setId(String id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setAssociated_user(String associated_user) { this.associated_user = associated_user; }
    public String getAssociated_user() { return associated_user; }
}
