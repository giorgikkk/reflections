package com.example.io;

import com.example.annotations.Column;
import com.example.annotations.CSV;
import com.example.exceptions.FieldNotFoundException;
import com.example.exceptions.HeaderNotFoundException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Helper class to read data from csv file.
 *
 * @param <D> data class. Relevant class needs to be annotated by @CSV
 */
public class CSVReader<D> {

    private final Class<? extends D> clazz;

    public CSVReader(Class<? extends D> clazz) {
        this.clazz = clazz;
    }

    /**
     * @return collection of read objects
     * @throws IOException if IO exception occurs
     */
    public List<D> readData() throws IOException, HeaderNotFoundException {
        Path path = Path.of(clazz.getAnnotation(CSV.class).source());

        final String[] headers = getHeaders(path);

        try(Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1)
                    .map(line -> line.split(","))
                    .map(line -> mapToD(line, headers))
                    .collect(Collectors.toList());
        }
    }


    /**
     * Read the data written on a particular line and return as a Java object.<br />
     * If no data found on the line, <code>Optional</code> will be empty.
     *
     * @param lineIndex the line index from which the data should be retrieved. 0 - header.
     * @return relevant class object,
     * @throws IllegalArgumentException if <code>lineIndex <= 0</code>.
     * @throws IOException              if IO exception occurs
     */
    public Optional<D> readLine(long lineIndex) throws IOException, HeaderNotFoundException {
        if(lineIndex <= 0) {
            throw new IllegalArgumentException("Illegal line number!!!");
        }

        Path path = Path.of(clazz.getAnnotation(CSV.class).source());

        final String[] headers = getHeaders(path);

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1)
                    .map(line -> line.split(","))
                    .map(line -> mapToD(line, headers))
                    .skip(lineIndex - 1)
                    .findFirst();
        }
    }


    /**
     * @param fromIndex start line index
     * @param toIndex   end line index(up to)
     * @return collection of objects read between specific lines
     * @throws IllegalArgumentException if <code>fromIndex <= 0 || fromIndex >= toIndex</code>.
     * @throws IOException              if IO exception occurs
     */
    public List<D> readLines(long fromIndex, long toIndex) throws IOException, HeaderNotFoundException {
        if(fromIndex <= 0 || fromIndex >= toIndex) {
            throw new IllegalArgumentException("Illegal line numbers!!!");
        }

        Path path = Path.of(clazz.getAnnotation(CSV.class).source());

        final String[] headers = getHeaders(path);


        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(line -> line.split(","))
                    .map(line -> mapToD(line, headers))
                    .skip(fromIndex - 1)
                    .limit(toIndex - 1)
                    .collect(Collectors.toList());
        }
    }

    private String[] getHeaders(Path path) throws IOException, HeaderNotFoundException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .limit(1)
                    .map(line -> line.split(","))
                    .filter(arr -> arr.length > 0)
                    .findFirst()
                    .orElseThrow(() -> new HeaderNotFoundException("No such headers!!!"));
        }

    }

    private D mapToD(String[] params, String[] headers) {
        Constructor<?> constructor = Stream.of(clazz.getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == params.length)
                .findFirst().orElseThrow();

        final Field[] fields = clazz.getDeclaredFields();

        final AtomicInteger index = new AtomicInteger();

        Object[] paramsToPass = Stream.of(headers)
                .map(h -> {
                    Field field;
                    try {
                        field = findField(fields, h);
                    } catch (FieldNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    return parse(field.getType().toString(),
                            params[index.getAndIncrement()],
                            field.getAnnotation(Column.class).dateFormat());
                })
                .toArray();

        try {
            return (D) constructor.newInstance(paramsToPass);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    private Field findField(Field[] field, String header) throws FieldNotFoundException {
        return Stream.of(field)
                .filter(f -> f.isAnnotationPresent(Column.class))
                .filter(f -> f.getAnnotation(Column.class).name().equals(header))
                .findFirst()
                .orElseThrow(() -> new FieldNotFoundException("No such Field!!!"));
    }


    private Object parse(String type, String param, String dateFormat) {
        switch (type) {
            case "int":
                return Integer.parseInt(param);

            case "double":
                return Double.parseDouble(param);

            case "long":
                return Long.parseLong(param);

            case "class java.time.LocalDateTime":
                return LocalDateTime.parse(param, DateTimeFormatter.ofPattern(dateFormat));

            case "class java.time.LocalDate":
                return LocalDate.parse(param, DateTimeFormatter.ofPattern(dateFormat));

            default:
                return param;
        }
    }
}
