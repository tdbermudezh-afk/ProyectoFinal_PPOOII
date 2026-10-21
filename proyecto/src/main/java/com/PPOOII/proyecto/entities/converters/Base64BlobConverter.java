package com.PPOOII.proyecto.entities.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;

/**
 * Convertidor JPA para almacenar cadenas Base64 de documentos PDF dentro de columnas tipo LONGBLOB en MySQL,
 * manteniendo la representación String en Java y en las respuestas/peticiones JSON.
 */
@Converter
public class Base64BlobConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        return new String(dbData, StandardCharsets.UTF_8);
    }
}

