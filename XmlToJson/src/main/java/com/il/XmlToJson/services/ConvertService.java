package com.il.XmlToJson.services;

import com.il.XmlToJson.model.ConvertedData;

public interface ConvertService {
    ConvertedData convertToJson(String xmlContent);
}
