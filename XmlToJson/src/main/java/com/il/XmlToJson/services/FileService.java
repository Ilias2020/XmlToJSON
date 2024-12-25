package com.il.XmlToJson.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;

@Service
public class FileService {

    @Value("${file.storage.path:./logs/}")
    private String storagePath;

    public String convertAndSaveXml(String xmlContent) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(xmlContent.getBytes());

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode wrappedNode = jsonMapper.createObjectNode().set("Data", jsonNode);

            String jsonContent = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrappedNode);

            String type = jsonNode.at("/Type").asText("Unknown");
            String date = jsonNode.at("/Creation/Date").asText(LocalDate.now().toString()).split("T")[0];

            String fileName = String.format("%s-%s.log", type, date);
            File file = new File(storagePath + File.separator + fileName);

            File directory = new File(storagePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            int recordCount = getRecordCount(file);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                if (recordCount == 0) {
                    writer.write("Records Count: 0" + System.lineSeparator());
                }
                writer.write(jsonContent + System.lineSeparator());
            }

            updateRecordCount(file, recordCount + 1);

            return String.format("XML converted and saved successfully. Records Count: %d", recordCount + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing XML: " + e.getMessage(), e);
        }
    }

    private int getRecordCount(File file) {
        if (!file.exists()) {
            return 0;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("Records Count:")) {
                return Integer.parseInt(firstLine.replace("Records Count:", "").trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
        return 0;
    }

    private void updateRecordCount(File file, int newCount) {
        try {
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String firstLine = reader.readLine();
                writer.write("Records Count: " + newCount + System.lineSeparator());
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + System.lineSeparator());
                }
            }

            if (!file.delete() || !tempFile.renameTo(file)) {
                throw new RuntimeException("Failed to update record count.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating record count in file: " + file.getName(), e);
        }
    }
}
