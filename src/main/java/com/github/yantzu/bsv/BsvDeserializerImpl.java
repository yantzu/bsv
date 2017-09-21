package com.github.yantzu.bsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.yantzu.bsv.BsvSchema.Field;

public class BsvDeserializerImpl implements BsvDeserializer {

    private final static Logger       LOG        = LoggerFactory.getLogger(BsvDeserializerImpl.class);
    

    private BsvContext                context;
    private BufferedReader            reader;
    
	private String majorVersion;
	private char minorVersion;
    
    //<variant, Schema>
    private Map<Character, BsvSchema> schemas;

    private StringBuilder             valueCache = new StringBuilder(512);


    protected BsvDeserializerImpl(BsvContext context, InputStream inputStream) throws IOException, BsvException {
        this(context, inputStream, BsvContext.DEFAULT_CHARSET);
    }


    protected BsvDeserializerImpl(BsvContext context, InputStream inputStream, Charset charset)
            throws IOException, BsvException {
        byte[] majorVersionByte = new byte[2];
        inputStream.read(majorVersionByte);

        majorVersion = new String(majorVersionByte, BsvContext.DEFAULT_CHARSET);
        minorVersion = (char) inputStream.read();
        
        int delimiter = inputStream.read();
//        inputStream.read();
        asserts(delimiter, context.getLineDelimiter());
        
        this.schemas = context.getSchemas(majorVersion, minorVersion);

        this.context = context;
        this.reader = new BufferedReader(new InputStreamReader(inputStream, charset));
	}


    @Override
    public BsvObject next() throws IOException, BsvException {
        try {
            return doNext();
        } catch (IOException ioException) {
            throw ioException;
        } catch (BsvException bsvException) {
            doSkip();
            throw bsvException;
        }
    }


    protected void doSkip() throws IOException {
        StringBuilder discardCache = new StringBuilder();
        while (true) {
            int i = reader.read();
            if (i == -1) {
                LOG.error("Discard raw data:" + discardCache.toString());
                break;
            }

            char c = (char) i;
            if (c == context.getLineDelimiter()) {
                LOG.error("Discard raw data:" + discardCache.toString());
                break;
            } else {
                discardCache.append(c);
            }
        }
    }
    

    protected BsvObject doNext() throws IOException, BsvException {
        int i = reader.read();
        if (i == -1) {
            return null;
        }

        char variantNumber = (char) i;
        BsvSchema schema = schemas.get(variantNumber);
        if (schema == null) {
            throw new BsvException("No schema defined for variant " + variantNumber);
        }
        
        BsvObject bean;
        try {
            bean = (BsvObject)schema.getBeanConstructor().newInstance();
            bean.setVersion(schema.getFullVersion());
        } catch (Exception exception) {
            throw new BsvException("Not able to initial bean instance due to "
                    + exception.getMessage(), exception);
        }

        
        try {
            Iterator<Field> fieldsIterator = schema.getFields().iterator();
            while (fieldsIterator.hasNext()) {
                Field field = fieldsIterator.next();
                Object fieldValue = nextField(field, fieldsIterator.hasNext());
                field.getBeanWriteMethod().invoke(bean, fieldValue);
            }
        } catch (Exception exception) {
            LOG.error("Discard object:" + bean.toString());
            if (exception instanceof BsvException) {
                throw (BsvException) exception;
            } else {
                throw new BsvException(exception);
            }
        }

        return bean;
    }

    
    private Object nextField(Field field, boolean hasMoreField) throws IOException, BsvException {
        Object result = null;
        int i;
        switch (field.getType()) {
            case STRING:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                result = valueCache.toString();
                break;
            case BOOLEAN:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                if (valueCache.length() == 1) {
                    if (valueCache.charAt(0) == '1') {
                        result = Boolean.TRUE;
                    } else {
                        result = Boolean.FALSE;
                    }
                } else {
                    result = Boolean.FALSE; //not allow other boolean encoding
                }
                break;
            case INTEGER:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                String integerStringValue = valueCache.toString();
                if (integerStringValue == null || integerStringValue.equals("")) {
                    result = null;
                } else {
                    result = Integer.valueOf(integerStringValue);
                }
                break;
            case LONG:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                String longStringValue = valueCache.toString();
                if (longStringValue == null || longStringValue.equals("")) {
                    result = null;
                } else {
                    result = Long.valueOf(longStringValue);
                }
                break;
            case FLOAT:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                String floatStringValue = valueCache.toString();
                if (floatStringValue == null || floatStringValue.equals("")) {
                    return null;
                } else {
                    result = Float.valueOf(floatStringValue);
                }
                break;
            case DOUBLE:
                i = nextToken();
                asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                String doubleStringValue = valueCache.toString();
                if (doubleStringValue == null || doubleStringValue.equals("")) {
                    return null;
                } else {
                    result = Double.parseDouble(doubleStringValue);
                }
                break;
            case LIST:
                List<String> list = new ArrayList<String>();
                while (true) {
                    i = nextToken();
                   
                    list.add(valueCache.toString());
                    if (i == context.getItemsDelimiter()) {
                        continue;
                    } else {
                        asserts(i, hasMoreField, context.getFieldsDelimiter(),
                                context.getLineDelimiter(), -1);
                        break;
                    }
                }
                result = list;
                break;
            case MAP:
                Map<String, String> map = new HashMap<String, String>();
                while (true) {
                    i = nextToken();
                    if (i == context.getKeyValueDelimiter()) {
                        String key = valueCache.toString();
                        
                        i = nextToken();
                        
                        map.put(key, valueCache.toString());
                        if (i == context.getItemsDelimiter()) {
                            continue;
                        } else {
                            asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                            break;
                        }
                    } else {
                        asserts(i, hasMoreField, context.getFieldsDelimiter(), context.getLineDelimiter(), -1);
                        break;
                    }
                }
                result = map;
                break;
        }
        return result;
    }

    /**
     * read next value in to cache
     * @return value ending token
     * @throws IOException
     */
    private int nextToken() throws IOException {
        valueCache.setLength(0);

        int tokenEnding;
        while (true) {
            int i = reader.read();
            if (i == -1) {
                tokenEnding = i;
                break;
            }

            char c = (char) i;
            if (c == context.getFieldsDelimiter() || c == context.getItemsDelimiter()
                    || c == context.getKeyValueDelimiter() || c == context.getLineDelimiter()) {
                tokenEnding = i;
                break;
            } else {
                Character transcode = context.transcodingDeser(c);
                if (transcode == null) {
                    valueCache.append(c);
                } else {
                    valueCache.append(transcode.charValue());
                }
            }
        }
        return tokenEnding;
    }

    private void asserts(int actual, boolean bool, int trueExpected, int falseExpected1,
                         int falseExpected2) throws BsvException {
        if (bool) {
            if (actual != trueExpected) {
                String errorMessage = "Invalid delimiter, expected " + trueExpected + ", but was "
                        + actual + ", cache is " + valueCache.toString();
                LOG.error(errorMessage);
                throw new BsvException(errorMessage);
            }
        } else {
            if (actual != falseExpected1 && actual != falseExpected2) {
                String errorMessage = "Invalid delimiter, expected " + falseExpected1 + " or "
                        + falseExpected2 + ", but was " + actual + ", cache is " + valueCache.toString();
                LOG.error(errorMessage);
                throw new BsvException(errorMessage);
            }
        }
    }
    
//    private void asserts(int actual, boolean bool, int trueExpected1, int trueExpected2, int falseExpected1, int falseExpected2) {
//        if (bool) {
//            if (actual != trueExpected1 && actual != trueExpected2) {
//                throw new BsvException("Invalid data, expecte " + trueExpected1 + " or "
//                        + trueExpected2 + ", but is " + actual);
//            }
//        } else {
//            if (actual != falseExpected1 && actual != falseExpected2) {
//                throw new BsvException("Invalid data, expecte " + falseExpected1 + " or "
//                        + falseExpected2 + ", but is " + actual);
//            }
//        }
//    }
    
    private void asserts(int actual, int expected) throws BsvException {
        if (actual != expected) {
            throw new BsvException("Invalid delimiter, expected " + expected + ", but was " + actual);
        }
    }


	@Override
	public void close() throws IOException, BsvException {
		reader.close();
	}
}
