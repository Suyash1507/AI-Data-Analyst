package com.example.backend.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.Repository.DataRepository;
import com.example.backend.dataset.Dataset;
import com.example.backend.dto.DatasetResponse;

import org.springframework.transaction.annotation.Transactional;
// import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Service 
public class Dataservice{
    
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv", "xlsx", "xls");

    

    private final DataRepository repository;
    private final Path uploadDirectory;

    public Dataservice(
            DataRepository repository,
            @Value ("${app.storage.upload-dir}") String uploadDir) 
    {
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
                    "Please upload a non-empty file."
            );
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException(
                    "The uploaded file must have a filename."
            );
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
                    "Only CSV, XLSX and XLS files are supported."
            );
        }

        // Use a generated filename for physical storage.
        String storageKey = UUID.randomUUID() + "." + extension;
        Path destination =
                uploadDirectory.resolve(storageKey).normalize();

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
                    Files.size(destination)
            );

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




    }




    
    





