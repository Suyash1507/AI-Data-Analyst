package com.example.backend.Controller;

import com.example.backend.Service.Dataservice;
import com.example.backend.dataset.DataPreviewResponse;
import com.example.backend.dto.DatasetResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/datasets")
public class DatasetController {
    
    
    private final Dataservice datasetService;

    public DatasetController(Dataservice datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DatasetResponse upload(
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return datasetService.upload(file);  
    }

    @GetMapping
    public List<DatasetResponse> getAllDatasets() {
        return datasetService.getAllDatasets();
    }

    @GetMapping("/{id}/preview")
    public DataPreviewResponse previewDataset(
                    @PathVariable UUID id,
                    @RequestParam(defaultValue = "20") int rows ) throws IOException {

                        return datasetService.preview(id, rows);

                }

    
}
