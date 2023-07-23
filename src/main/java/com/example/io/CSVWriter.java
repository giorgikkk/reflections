package com.example.io;

import com.example.annotations.Column;
import com.example.annotations.CSV;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVWriter<D> {

    private final Class<? extends D> clazz;

    public CSVWriter(Class<? extends D> clazz) {
        this.clazz = clazz;
    }

    /**
     * Only that data will be stored in the file.<br />
     * First line will be header row.
     *
     * @param data data that we want to be written in csv file.
     */
    public void writeData(Iterable<D> data) {
        Path path = Path.of(clazz.getAnnotation(CSV.class).source());

        List<String> headers = getHeaders();
        writeHeaders(headers, path);

        data.forEach(d -> {
            try {
                appendData(d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeHeaders(List<String> headers, Path path) {
        headers.forEach(h -> {
            try {
                Files.write(path,
                        (String.join(",", h) + "\n").getBytes(),
                        StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<String> getHeaders() {
        return Stream.of(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> f.getAnnotation(Column.class).name())
                .collect(Collectors.toList());
    }

    /**
     * Adds the param data to the existing ones.<br />
     * Header rows won't be added.
     *
     * @param data data that we want to be written in csv file.
     */
    public void appendData(Iterable<D> data) {
        data.forEach(d -> {
            try {
                appendData(d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Adds the param data to the existing ones.<br />
     * Header rows won't be added.
     *
     * @param data data that we want to be written in csv file.
     * @throws IOException - if IO exception occurs
     */
    public void appendData(D data) throws IOException {
        Path path = Path.of(clazz.getAnnotation(CSV.class).source());

        try {
            Files.write(path,
                    (getData(data) + "\n").getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String getData(D d) throws InvocationTargetException, IllegalAccessException {
        StringBuilder data = new StringBuilder();

        Field[] fields = d.getClass().getDeclaredFields();
        Stream.of(fields)
                .filter(f -> f.isAnnotationPresent(Column.class))
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        data.append(f.get(d)).append(",");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        data.setLength(data.length() - 1);

        return data.toString();
    }
}
