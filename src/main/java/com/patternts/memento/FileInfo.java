package com.patternts.memento;

import java.util.Date;

public class FileInfo {
    private String fileText;
    private Date date;

    public void setFileTextAndDate(String fileText, Date date) {
        this.fileText = fileText;
        this.date = date;
    }

    public FileInfoMemento save() {
        return new FileInfoMemento(fileText, date);
    }

    public void load(FileInfoMemento fileInfoMemento) {
        fileText = fileText;
        date = fileInfoMemento.getDate();
    }

}
