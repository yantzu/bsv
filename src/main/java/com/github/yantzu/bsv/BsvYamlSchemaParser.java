package com.github.yantzu.bsv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.yantzu.bsv.BsvSchema.FieldType;

public class BsvYamlSchemaParser implements BsvSchemaParser {

    @Override
    public BsvSchema parse(InputStream inputStream) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> config = mapper.readValue(inputStream, TypeFactory
                    .defaultInstance().constructMapType(Map.class, String.class, Object.class));

            BsvSchemaImpl schema = new BsvSchemaImpl()
                .withMajorVersion(config.get("majorVersion").toString())
                .withMinorVersion(config.get("minorVersion").toString().charAt(0))
                .withVariantNumber(config.get("variantNumber").toString().charAt(0))
                .withBeanClass(Class.forName(config.get("beanClass").toString()));
            
            @SuppressWarnings("unchecked")
            Iterable<Map<String, String>> fields = (Iterable<Map<String, String>>) config.get("fields");
            Iterator<Map<String, String>>  fieldsIterator = fields.iterator();
            
            int index = 0;
            while(fieldsIterator.hasNext()) {
                Map<String, String> field = fieldsIterator.next();
                schema.withField(index++, field.get("name"), asFieldType(field.get("type")));
            }
            
            return schema;
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalArgumentException("Invalid config:"
                    + classNotFoundException.getMessage(), classNotFoundException);
        } catch (JsonParseException jsonParseException) {
            throw new IllegalArgumentException(jsonParseException.getMessage(), jsonParseException);
        }
    }
    

    private FieldType asFieldType(String type) {
        FieldType result = FieldType.valueOfIgnoreCase(type);

        if (result == null) {
            String lowercaseType = type.toLowerCase(Locale.ENGLISH);
            if ("int".equals(lowercaseType)) {
                result = FieldType.INTEGER;
            } else if ("bool".equals(lowercaseType)) {
                result = FieldType.BOOLEAN;
            } else if ("array".equals(lowercaseType)) {
                result = FieldType.LIST;
            }
        }
        return result;
    }

}
