package com.github.bsv;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BsvSchemaImpl implements BsvSchema {
    private String      majorVersion;
    private char        minorVersion;
    private char        variantNumber;

    private String         fullVersion;
    
    private List<Field> fields = new ArrayList<Field>();

    private Class<?>       beanClass;
    private Constructor<?> beanConstructor;
    private BeanInfo       beanInfo;
    
    
    @Override
    public String getMajorVersion() {
        return majorVersion;
    }

    @Override
    public char getMinorVersion() {
        return minorVersion;
    }
    
    @Override
    public String getFullVersion() {
        return fullVersion;
    }

    @Override
    public char getVariantNumber() {
        return variantNumber;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Constructor<?> getBeanConstructor() {
        return beanConstructor;
    }
    
    public BsvSchemaImpl withMajorVersion(String majorVersion) {
        if (majorVersion.length() != 2) {
            throw new IllegalArgumentException("majorVersion must be 2 chars, but is "
                    + majorVersion);
        }
        this.majorVersion = majorVersion;
        
        if(this.minorVersion != '\0') {
            fullVersion = this.majorVersion + "." + this.minorVersion;
        }
        
        return this;
    }


    public BsvSchemaImpl withMinorVersion(char minorVersion) {
        this.minorVersion = minorVersion;
        if (this.majorVersion != null && !this.majorVersion.equals("")) {
            fullVersion = this.majorVersion + "." + this.minorVersion;
        }
        return this;
    }


    public BsvSchemaImpl withVariantNumber(char variantNumber) {
        this.variantNumber = variantNumber;
        return this;
    }
    
    public BsvSchemaImpl withField(int index, String name, FieldType type) {
        if(this.beanClass == null || this.beanInfo == null) {
            throw new IllegalStateException("Please set beanClass before add field"); 
        }
        Method beanWriteMethod = null;
        for (PropertyDescriptor propertyDescriptor : this.beanInfo.getPropertyDescriptors()) {
            if(propertyDescriptor.getName().equals(name)) {
                beanWriteMethod = propertyDescriptor.getWriteMethod();
                break;
            }
        }
        if (beanWriteMethod == null) {
            throw new IllegalArgumentException("No property " + name + " exist in class "
                    + this.beanClass);
        }
        this.fields.add(new FieldImpl(index, name, type, beanWriteMethod));
        return this;
    }
    
    public BsvSchemaImpl withBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        try {
            this.beanConstructor = beanClass.getConstructor(String.class);
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new IllegalStateException("Class " + beanClass + " has no constructor with 1 String argument");
        } catch (SecurityException securityException) {
            throw new IllegalStateException("Has no athority to access Class " + beanClass);
        }
        try {
            this.beanInfo = Introspector.getBeanInfo(this.beanClass);
        } catch (IntrospectionException introspectionException) {
            throw new IllegalStateException("Class " + beanClass + " is not a valid bean", introspectionException);
        }
        return this;
    }

    private static class FieldImpl implements BsvSchema.Field {
        private int       index;
        private FieldType type;
        private String    name;
        private Method    beanWriteMethod;

        public FieldImpl(int index, String name, FieldType type, Method beanWriteMethod) {
            super();
            this.index = index;
            this.type = type;
            this.name = name;
            this.beanWriteMethod = beanWriteMethod;
        }

        public int getIndex() {
            return index;
        }

        public FieldType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Method getBeanWriteMethod() {
            return beanWriteMethod;
        }
    }

}
