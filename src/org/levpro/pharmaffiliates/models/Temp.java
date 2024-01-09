package org.levpro.pharmaffiliates.models;

import java.util.ArrayList;

public class Temp {
    private ArrayList<Childrens> childrens;
    private ArrayList<String> parsed;

    public Temp () {
        this.childrens = new ArrayList<>();
        this.parsed = new ArrayList<>();
    }

    public ArrayList<Childrens> getChildrens() {
        return childrens;
    }

    public void setChildrens(ArrayList<Childrens> childrens) {
        this.childrens = childrens;
    }

    public void addChildren(Childrens childrens) {
        this.childrens.add(childrens);
    }

    public ArrayList<String> getParsed() {
        return parsed;
    }

    public void setParsed(ArrayList<String> parsed) {
        this.parsed = parsed;
    }

    public void addParsed(String parsed) {
        this.parsed.add(parsed);
    }
}
