package com.il.XmlToJson.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.il.XmlToJson.model.ConvertedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ConvertServiceImpl implements ConvertService {
    Logger logger = LogManager.getLogger(ConvertServiceImpl.class);

    @Override
    public ConvertedData convertToJson(String xmlContent) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(xmlContent.getBytes());

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode wrappedNode = jsonMapper.createObjectNode().set("Data", jsonNode);

            String type = jsonNode.at("/Type").asText("Unknown");
            String date = jsonNode.at("/Creation/Date").asText(LocalDate.now().toString()).split("T")[0];
            String data = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrappedNode);

            return new ConvertedData(type, date, data);

        } catch (Exception e) {
            logger.error("Failed while converting into XML" + e.getMessage());
            return null;
        }
    }
}
