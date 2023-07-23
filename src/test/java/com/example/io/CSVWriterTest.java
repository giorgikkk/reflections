package com.example.io;


import com.example.Data;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CSVWriterTest {
    private final static Path PATH = Path.of(Data.SOURCE_FILE);

    @Before
    public void beforeEach() throws IOException {
        Files.deleteIfExists(PATH);
    }

    @Test
    public void testWriteData() throws IOException {

        CSVWriter<Data> writer = new CSVWriter<>(Data.class);
        writer.writeData(List.of(new Data(1)));

        final int size = Files.readAllLines(PATH).size();
        final int expected = 2;

        assertEquals(expected, size);

    }

    @Test
    public void testAppendData() throws IOException {

        CSVWriter<Data> writer = new CSVWriter<>(Data.class);
        writer.appendData(List.of(new Data(1)));

        final int size = Files.readAllLines(PATH).size();
        final int expected = 1;

        assertEquals(expected, size);
    }

    @Test
    public void testTestAppendData() throws IOException {

        CSVWriter<Data> writer = new CSVWriter<>(Data.class);
        writer.appendData(new Data(1));

        final int size = Files.readAllLines(PATH).size();
        final int expected = 1;

        assertEquals(expected, size);
    }
}