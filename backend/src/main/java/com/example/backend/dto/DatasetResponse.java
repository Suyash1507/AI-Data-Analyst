package com.example.backend.dto;

import java.time.Instant;
import java.util.UUID;

import com.example.backend.dataset.Dataset;

public record DatasetResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String status,
        Instant uploadedAt
) {
    public static DatasetResponse from(Dataset dataset) {
        return new DatasetResponse(
                dataset.getId(),
                dataset.getOriginalFilename(),
                dataset.getContentType(),
                dataset.getSizeBytes(),
                dataset.getStatus(),
                dataset.getUploadedAt()
        );
    }
}
