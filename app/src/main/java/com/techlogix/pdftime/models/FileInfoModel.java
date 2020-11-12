package com.techlogix.pdftime.models;

import java.io.File;

public class FileInfoModel {
    String fileName, fileType;
    File file;
    Boolean isSelect = false;

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public FileInfoModel(String fileName, String fileType, File file) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
