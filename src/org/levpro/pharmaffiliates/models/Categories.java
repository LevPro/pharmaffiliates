package org.levpro.pharmaffiliates.models;

public class Categories {
    private String url;
    private String name;
    private String description;
    private String parent;

    public Categories(String url, String name, String description, String parent) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.parent = parent;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getParent() {
        return parent;
    }
}
