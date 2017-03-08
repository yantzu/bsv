package com.github.bsv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BSV stands for Binary-Separated Values
 * 
 * @author yanxilang
 */
public class BsvContext {
    //<major.minor, <variant, Schema>>
    private Map<String, Map<Character, BsvSchema>> schemas;
    private char                                   fieldsDelimiter;
    private char                                   itemsDelimiter;
    private char                                   keyValueDelimiter;
    private char                                   lineDelimiter;

	private Map<Character, Character> transcodes = new HashMap<Character, Character>();
	
    
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
		this.transcodes = transcodes;
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

	protected Character transcoding(Character from) {
		return transcodes.get(from);
	}
	
    protected Map<Character, BsvSchema> getSchemas(String majorVersion, char minorVersion) {
        Map<Character, BsvSchema> result = schemas.get(getFullVersion(majorVersion, minorVersion));
        if (result == null) {
            throw new IllegalArgumentException("No schemas defined for " + majorVersion + '.'
                    + minorVersion);
        }
        return result;
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
