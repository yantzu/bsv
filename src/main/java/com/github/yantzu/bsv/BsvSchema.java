package com.github.yantzu.bsv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public interface BsvSchema {
    String getMajorVersion(); //two char

    char getMinorVersion();
    
    String getFullVersion();
    
    char getVariantNumber();

    List<Field> getFields();

    Class<?> getBeanClass();
    Constructor<?> getBeanConstructor();
    
    public enum FieldType {
        BOOLEAN,
        STRING,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        LIST,
        MAP;
        
        public static FieldType valueOfIgnoreCase(String type) {
            for (FieldType fieldType : FieldType.values()) {
                if (fieldType.name().equalsIgnoreCase(type)) {
                    return fieldType;
                }
            }
            return null;
        }
    }

    public static interface Field {
        int getIndex();

        FieldType getType();

        String getName();
        
        Method getBeanWriteMethod();
    }
}
