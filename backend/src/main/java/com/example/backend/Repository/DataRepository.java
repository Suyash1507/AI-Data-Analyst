package com.example.backend.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.dataset.Dataset;

public interface DataRepository extends JpaRepository<Dataset,UUID>{
    
}
