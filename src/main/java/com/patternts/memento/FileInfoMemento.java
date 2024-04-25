package com.patternts.memento;

import java.util.Date;

public class FileInfoMemento {
    private final String fileText;
    private final Date date;

    public FileInfoMemento(String fileText, Date date) {
        this.fileText = fileText;
        this.date = date;
    }

    public String getFileText() {
        return fileText;
    }

    public Date getDate() {
        return date;
    }
}
