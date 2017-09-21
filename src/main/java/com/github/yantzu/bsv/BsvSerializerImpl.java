package com.github.yantzu.bsv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.yantzu.bsv.BsvSchema.Field;

public class BsvSerializerImpl implements BsvSerializer {

	private final static Logger       LOG        = LoggerFactory.getLogger(BsvSerializerImpl.class);
	
	private BsvContext context;
	private BufferedWriter writer;

	private String majorVersion;
	private char minorVersion;
	
	 //<variant, Schema>
    private Map<Character, BsvSchema> schemas;
    
	protected BsvSerializerImpl(BsvContext context, OutputStream outputStream, String majorVersion, char minorVersion)
			throws IOException, BsvException {
		this(context, outputStream, majorVersion, minorVersion, BsvContext.DEFAULT_CHARSET);
	}

	protected BsvSerializerImpl(BsvContext context, OutputStream outputStream, String majorVersion, char minorVersion,
			Charset charset) throws IOException, BsvException {
		byte[] majorVersionByte = majorVersion.getBytes(BsvContext.DEFAULT_CHARSET);
		if (majorVersionByte.length != 2) {
			throw new BsvException("Major version:" + majorVersion + "is not two bytes");
		}

		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		
		outputStream.write(majorVersionByte);
		outputStream.write(minorVersion);
		outputStream.write(context.getLineDelimiter());

		this.schemas = context.getSchemas(majorVersion, minorVersion);

		this.context = context;
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
	}
	    
	@Override
	public void next(BsvObject bean) throws IOException, BsvException {
		if (bean.getVersion().length() != 6 || !bean.getVersion().startsWith(majorVersion)
				|| bean.getVersion().charAt(3) != minorVersion) {
			throw new BsvException("Invalid version:" + bean.getVersion());
		}

		char variantNumber = bean.getVersion().charAt(5);
		BsvSchema schema = this.schemas.get(variantNumber);
		if (schema == null) {
			throw new BsvException("No schema defined for variant " + variantNumber);
		}
		
		writer.write(variantNumber);
		
		try {
			Iterator<Field> fieldsIterator = schema.getFields().iterator();
			while (fieldsIterator.hasNext()) {
				Field field = fieldsIterator.next();
				Object fieldValue = field.getBeanReadMethod().invoke(bean);
				writeField(field, fieldValue, fieldsIterator.hasNext());
			}
		} catch (Exception exception) {
			LOG.error("Discard object:" + bean.toString());
			if (exception instanceof BsvException) {
				throw (BsvException) exception;
			} else {
				throw new BsvException(exception);
			}
		}
	}

	
    private void writeField(Field field, Object fieldValue, boolean hasMoreField) throws IOException, BsvException {
        switch (field.getType()) {
            case STRING:
				char[] stringFieldValue;
				if (fieldValue == null) {
					stringFieldValue = "".toCharArray();
				} else {
					stringFieldValue = ((String) fieldValue).toCharArray();
				}
				
				transcode(stringFieldValue);
				writer.write(stringFieldValue);
                break;
            case BOOLEAN:
            	Boolean booleanFieldValue = (Boolean) fieldValue;
            	if (booleanFieldValue != null && booleanFieldValue) {
					writer.write('1');
				} else {
					writer.write('0');
				}
                break;
            case INTEGER:
				if (fieldValue == null) {
					fieldValue = 0;
				}
				writer.write(((Integer) fieldValue).toString());
				break;
            case LONG:
            	if (fieldValue == null) {
					fieldValue = 0l;
				}
				writer.write(((Long) fieldValue).toString());
				break;
            case FLOAT:
            	if (fieldValue == null) {
					fieldValue = 0f;
				}
				writer.write(((Float) fieldValue).toString());
				break;
            case DOUBLE:
            	if (fieldValue == null) {
					fieldValue = 0d;
				}
				writer.write(((Double) fieldValue).toString());
				break;
            case LIST:
            	@SuppressWarnings("unchecked")
            	Iterable<String> list = (Iterable<String>) fieldValue;
            	Iterator<String> listIterator;
				if (list == null) {
					listIterator = new ArrayList<String>(0).iterator();
				} else {
					listIterator = list.iterator();
				}
                
            	while (listIterator.hasNext()) {
            		char[] itemValue = listIterator.next().toCharArray();
    				transcode(itemValue);
    				writer.write(itemValue);
					
					if (listIterator.hasNext()) {
						writer.write(context.getItemsDelimiter());
					}
                }
                break;
            case MAP:
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) fieldValue;
                Iterator<Entry<String, String>> mapIterator; 
				if (map == null) {
					mapIterator = new HashMap<String, String>(0).entrySet().iterator();
				} else {
					mapIterator = map.entrySet().iterator();
				}
                
                while(mapIterator.hasNext()) {
                	Entry<String, String> entry = mapIterator.next();
                	
                	char[] keyValue = entry.getKey().toCharArray();
    				transcode(keyValue);
    				writer.write(keyValue);
					writer.write(context.getKeyValueDelimiter());
					char[] valueValue = entry.getValue().toCharArray();
    				transcode(valueValue);
    				writer.write(valueValue);
					
					if (mapIterator.hasNext()) {
						writer.write(context.getItemsDelimiter());
					}
                }
                break;
        }
        
		if (hasMoreField) {
			writer.write(context.getFieldsDelimiter());
		} else {
			writer.write(context.getLineDelimiter());
		}
    }
    
	private void transcode(char[] chars) {
		if (chars.length > 0) {
			for (int i = 0; i < chars.length; i++) {
				Character transcode = context.transcodingSer(chars[i]);
				if (transcode != null) {
					chars[i] = transcode;
				}
			}
		}
	}
    
	@Override
	public void close() throws IOException, BsvException {
		this.writer.close();
	}

}
