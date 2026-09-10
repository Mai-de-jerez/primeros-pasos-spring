package com.primera_app_spring.controller;

import com.primera_app_spring.security.CustomUserDetails;
import com.primera_app_spring.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MediaController {

    private final StorageService storageService;

    public MediaController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename, 
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // Verificación directa desde la sesión guardada en Redis
        if (userDetails == null || !filename.equals(userDetails.getFoto())) {
            return ResponseEntity.status(403).build();
        }

        Resource recurso = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(recurso);
    }
}
