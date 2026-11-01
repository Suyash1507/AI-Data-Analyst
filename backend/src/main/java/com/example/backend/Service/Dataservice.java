package com.example.backend.Service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.apache.poi.ss.usermodel.*;

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Repository.DataRepository;
import com.example.backend.dataset.DataPreviewResponse;
import com.example.backend.dataset.Dataset;
import com.example.backend.dto.DatasetResponse;

@Service
public class Dataservice {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv", "xlsx", "xls");

    private final DataRepository repository;
    private final Path uploadDirectory;

    public Dataservice(
            DataRepository repository,
            @Value("${app.storage.upload-dir}") String uploadDir) {
        this.repository = repository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Ensure the directory exists on startup
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Transactional
    public DatasetResponse upload(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a non-empty file.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException(
                    "The uploaded file must have a filename.");
        }

        // Do not trust any directory components in the supplied filename.
        originalName = Paths.get(originalName)
                .getFileName()
                .toString();

        int dot = originalName.lastIndexOf('.');
        String extension = dot < 0
                ? ""
                : originalName.substring(dot + 1)
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only CSV, XLSX and XLS files are supported.");
        }

        // Use a generated filename for physical storage.
        String storageKey = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storageKey).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        try {
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }

            Dataset dataset = new Dataset(
                    originalName,
                    storageKey,
                    file.getContentType(),
                    Files.size(destination));

            Dataset saved = repository.save(dataset);
            return DatasetResponse.from(saved);

        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<DatasetResponse> getAllDatasets() {
        return repository.findAll()
                .stream()
                .map(DatasetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DataPreviewResponse preview(UUID datasetId, int requestedRows) throws IOException {

        Dataset dataset = repository.findById(datasetId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found")
                );

        int limit = Math.max(1, Math.min(requestedRows, 100));

        Path filePath = uploadDirectory
                .resolve(dataset.getStorageKey())
                .normalize();

        if (!filePath.startsWith(uploadDirectory) || !Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file format");
        }

        String filename = dataset.getStorageKey().toLowerCase(Locale.ROOT);

        if (filename.endsWith(".csv")) {
            return previewCsv(dataset, filePath, limit);
        }

        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return previewExcel(dataset, filePath, limit);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file format");
    }

    private DataPreviewResponse previewCsv(Dataset dataset, Path filePath, int limit) throws IOException {
        try (Reader reader = Files.newBufferedReader(filePath);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .get()
                     .parse(reader)) {

            List<String> columns = new ArrayList<>(parser.getHeaderNames());
            List<List<String>> rows = new ArrayList<>();

            for (CSVRecord record : parser) {
                if (rows.size() >= limit) {
                    break;
                }

                List<String> row = new ArrayList<>();
                for (String column : columns) {
                    row.add(record.isMapped(column)
                            ? record.get(column)
                            : "");
                }
                rows.add(row);
            }

            return new DataPreviewResponse(
                    dataset.getId(),
                    columns,
                    rows,
                    rows.size()
            );
        }
    }

    private DataPreviewResponse previewExcel(Dataset dataset, Path filePath, int limit) throws IOException {
        try (InputStream input = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(input)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel workbook has no sheets."
                );
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.ROOT);

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Excel file does not contain a header row."
                );
            }

            int columnCount = Math.max(0, headerRow.getLastCellNum());
            List<String> columns = new ArrayList<>();

            for (int c = 0; c < columnCount; c++) {
                String columnName = formatter.formatCellValue(headerRow.getCell(c)).trim();
                columns.add(columnName.isBlank()
                        ? "column_" + (c + 1)
                        : columnName);
            }

            List<List<String>> rows = new ArrayList<>();

            for (int r = headerRow.getRowNum() + 1;
                 r <= sheet.getLastRowNum() && rows.size() < limit;
                 r++) {

                Row excelRow = sheet.getRow(r);
                List<String> values = new ArrayList<>();

                for (int c = 0; c < columnCount; c++) {
                    String value = excelRow == null
                            ? ""
                            : formatter.formatCellValue(excelRow.getCell(c));
                    values.add(value);
                }

                rows.add(values);
            }

            return new DataPreviewResponse(
                    dataset.getId(),
                    columns,
                    rows,
                    rows.size()
            );
        }
    }
}
