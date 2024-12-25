package com.il.XmlToJson.controllers;

import com.il.XmlToJson.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class XmlToJsonController {

    @Autowired
    private FileService fileService;

    @PostMapping("/process")
    public ResponseEntity<String> processXml(@RequestBody String xmlContent) {
        try {
            String result = fileService.convertAndSaveXml(xmlContent);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error processing XML: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}
