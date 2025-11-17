package com.elearning.service.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller để serve audio files
 */
@RestController
@RequestMapping("/api/audio")
@Slf4j
public class AudioController {

    @Value("${app.audio.storage.path:./audio}")
    private String audioStoragePath;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveAudioFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(audioStoragePath).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                log.warn("File âm thanh không tồn tại: {}", fileName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi serve audio file: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}