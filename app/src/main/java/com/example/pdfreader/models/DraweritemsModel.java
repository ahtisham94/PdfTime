package com.example.pdfreader.models;

public class DraweritemsModel {
    private String title;
    private int icon, type;
    private boolean isSeleted;

    public boolean isSeleted() {
        return isSeleted;
    }

    public void setSeleted(boolean seleted) {
        isSeleted = seleted;
    }

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

    public DraweritemsModel(String title, int icon, int type, boolean isSeleted) {
        this.title = title;
        this.icon = icon;
        this.type = type;
        this.isSeleted = isSeleted;
    }
}
