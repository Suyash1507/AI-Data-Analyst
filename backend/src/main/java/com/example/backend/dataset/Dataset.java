package com.example.backend.dataset;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "datasets")
@AllArgsConstructor

public class Dataset {

    @Id
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private String status;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    protected Dataset() {
    }

    public Dataset(String originalFileName, String storageKey, String contentType, long sizeBytes) {
        this.id = UUID.randomUUID();
        this.originalFileName = originalFileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = "UPLOADED";
        this.uploadedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

}
