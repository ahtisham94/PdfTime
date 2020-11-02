package com.techlogix.pdftime.models;

public class DraweritemsModel {
    private String title;
    private int icon, type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DraweritemsModel(String title, int icon, int type) {
        this.title = title;
        this.icon = icon;
        this.type = type;
    }
}
