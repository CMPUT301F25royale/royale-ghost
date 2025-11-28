package com.example.project_part_3.Image;


import java.util.UUID;

public class ImageMetadata {
    private String id;
    private String url;
    private String type;
    private String description;
    private String owner;

    public ImageMetadata() {
    }

    public ImageMetadata(String url, String type, String description, String owner) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.type = type;
        this.description = description;
        this.owner = owner;
    }

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
}
