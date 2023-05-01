package com.akw.crimson.Backend.AppObjects;


public class FolderFacer {

    private String path,ID,folderName;
    private int imgCount,vidCount,iconType;
    private long icon,size;

    public FolderFacer(String path, String ID, String folderName, long icon, long size) {
        this.path = path;
        this.ID = ID;
        this.folderName = folderName;
        this.icon = icon;
        this.size = size;
    }



    public int getImgCount() {
        return imgCount;
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }
    public void incImgCount() {
        this.imgCount++;
    }
    public int getVidCount() {
        return vidCount;
    }

    public void setVidCount(int vidCount) {
        this.vidCount = vidCount;
    }

    public int getIconType() {
        return iconType;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public void incVidCount() {
        this.vidCount++;
    }

    public String getCount() {
        return (getImgCount()==0?"":getImgCount()>1?getImgCount()+" Photos ":"1 Photo ")+(getVidCount()==0?"":getVidCount()>1?getVidCount()+" Videos":"1 Video");
    }

    public void incSize(long size) {
        this.size += size;
    }

    public long getSize() {
        return size;
    }

    public String getSizeValue() {
        if (size >= 1073741824) {
            return String.format("%.2f GB", size / 1073741824.0);
        } else if (size >= 1048576) {
            return String.format("%.2f MB", size / 1048576.0);
        } else if (size >= 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return size + " B";
        }
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getIcon() {
        return icon;
    }

    public void setIcon(long icon) {
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