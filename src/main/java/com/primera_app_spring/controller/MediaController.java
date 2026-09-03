package com.primera_app_spring.controller;

import com.primera_app_spring.model.User;
import com.primera_app_spring.services.UserService;
import com.primera_app_spring.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class MediaController {

    private final UserService userService;
    private final StorageService storageService;

    public MediaController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename, Principal principal) {
        User usuario = userService.buscarPorUsername(principal.getName());

        if (!filename.equals(usuario.getFoto())) {
            return ResponseEntity.status(403).build();
        }

        Resource recurso = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(recurso);
    }
}
