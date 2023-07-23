package com.example;

import com.example.annotations.Column;
import com.example.annotations.CSV;

@CSV(source = Data.SOURCE_FILE)
public class Data {
    public static final String SOURCE_FILE = "./src/test/resources/a.csv";

    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    public Data(int id) {
        this.id = id;
    }

    public Data(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Data(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
