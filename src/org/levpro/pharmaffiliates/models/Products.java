package org.levpro.pharmaffiliates.models;

import java.util.HashMap;
import java.util.Map;

public class Products {
    private String url;
    private String name;
    private String image;
    private String description;
    private String searchKeywords;
    private String category;
    private Map<String, String> params;

    public Products(String url, String category, String name, String image) {
        this.url = url;
        this.category = category;
        this.name = name;
        this.image = image;
        this.params = new HashMap<>();
    }

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSearchKeywords() {
        return this.searchKeywords;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public void addParam(String name, String value) {
        this.params.put(name, value);
    }
}
