package com.il.XmlToJson.services;

import com.il.XmlToJson.model.ConvertedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.storage.path}")
    private String storagePath;

    private final ConvertService convertService;
    Logger logger = LogManager.getLogger(FileServiceImpl.class);

    public FileServiceImpl(ConvertService convertService) {
        this.convertService = convertService;
    }

    @Override
    public String convertAndSaveXml(String xmlContent) {
        try {
            ConvertedData convertedData = convertService.convertToJson(xmlContent);

            String fileName = String.format("%s-%s.log", convertedData.getType(), convertedData.getDate());
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
                writer.write(convertedData.getData() + System.lineSeparator());
            }

            updateRecordCount(file, recordCount + 1);

            return String.format("XML converted and saved successfully. Records Count: %d", recordCount + 1);
        } catch (Exception e) {
            logger.error("Processing XML failed");
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

                reader.readLine();
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
