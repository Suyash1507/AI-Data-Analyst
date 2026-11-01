package com.example.backend.dataset;

import java.util.List;
import java.util.UUID;

public record DataPreviewResponse(
    UUID datasetId,
    List<String> columns,
    List<List<String>> rows,
    long rowsReturned
) {
} 
