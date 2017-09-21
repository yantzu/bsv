package com.github.yantzu.bsv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * BSV stands for Binary-Separated Values
 * 
 * @author yanxilang
 */
public class BsvContext {
	
	protected static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
    //<major.minor, <variant, Schema>>
    private Map<String, Map<Character, BsvSchema>> schemas;
    private char                                   fieldsDelimiter;
    private char                                   itemsDelimiter;
    private char                                   keyValueDelimiter;
    private char                                   lineDelimiter;

    
    private Map<Character, Character> transcodesSer = new HashMap<Character, Character>();
	private Map<Character, Character> transcodesDeser = new HashMap<Character, Character>();
	
    
	protected BsvContext(List<BsvSchema> schemas, char fieldsDelimiter, char itemsDelimiter, char keyValueDelimiter,
			char lineDelimiter, Map<Character, Character> transcodes) {
        super();
        
        this.schemas = new HashMap<String, Map<Character, BsvSchema>>();
        for(BsvSchema schema : schemas) {
            String version = getFullVersion(schema.getMajorVersion(), schema.getMinorVersion());
            Map<Character, BsvSchema> schemasOfVersion = this.schemas.get(version);
            if(schemasOfVersion == null) {
                schemasOfVersion = new HashMap<Character, BsvSchema>();
                this.schemas.put(version, schemasOfVersion);
            }
            schemasOfVersion.put(schema.getVariantNumber(), schema);
        }
        
        this.fieldsDelimiter = fieldsDelimiter;
        this.itemsDelimiter = itemsDelimiter;
        this.keyValueDelimiter = keyValueDelimiter;
        this.lineDelimiter = lineDelimiter;
		this.transcodesSer = transcodes;
		for (Entry<Character, Character> transcode : transcodes.entrySet()) {
			this.transcodesDeser.put(transcode.getValue(), transcode.getKey());
		}
    }

    protected char getFieldsDelimiter() {
        return fieldsDelimiter;
    }

    protected char getItemsDelimiter() {
        return itemsDelimiter;
    }

    protected char getKeyValueDelimiter() {
        return keyValueDelimiter;
    }

    protected char getLineDelimiter() {
        return lineDelimiter;
    }

	protected Character transcodingSer(Character from) {
		return transcodesSer.get(from);
	}
	
	protected Character transcodingDeser(Character from) {
		return transcodesDeser.get(from);
	}
	
    protected Map<Character, BsvSchema> getSchemas(String majorVersion, char minorVersion) {
        Map<Character, BsvSchema> result = schemas.get(getFullVersion(majorVersion, minorVersion));
        if (result == null) {
            throw new IllegalArgumentException("No schemas defined for " + majorVersion + '.'
                    + minorVersion);
        }
        return result;
    }

	public BsvSerializer createSerializer(OutputStream outputStream, String majorVersion, char minorVersion)
			throws IOException, BsvException {
		return new BsvSerializerImpl(this, outputStream, majorVersion, minorVersion);
	}

	public BsvSerializer createSerializer(OutputStream outputStream, String majorVersion, char minorVersion,
			Charset charset) throws IOException, BsvException {
		return new BsvSerializerImpl(this, outputStream, majorVersion, minorVersion, charset);
	}
    
	
    public BsvDeserializer createDeserializer(InputStream inputStream) throws IOException, BsvException {
        return new BsvDeserializerImpl(this, inputStream);
    }

    public BsvDeserializer createDeserializer(InputStream inputStream, Charset charset)
            throws IOException, BsvException {
        return new BsvDeserializerImpl(this, inputStream, charset);
    }

    private String getFullVersion(String majorVersion, char minorVersion) {
        return majorVersion + '.' + minorVersion;
    }
}
