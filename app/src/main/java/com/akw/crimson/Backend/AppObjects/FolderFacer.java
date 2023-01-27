package com.akw.crimson.Backend.AppObjects;


public class FolderFacer {

    private String path;
    private String ID;
    private String folderName;
    private String icon;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public FolderFacer() {

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public FolderFacer(String path, String folderName) {
        this.path = path;
        this.folderName = folderName;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}