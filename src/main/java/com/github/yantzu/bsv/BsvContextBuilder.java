package com.github.yantzu.bsv;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BSV stands for Binary-Separated Values
 * 
 * @author yanxilang
 */
public class BsvContextBuilder {
    
    private List<BsvSchema> schemas           = new LinkedList<BsvSchema>();
    private char         fieldsSeperator   = 0x01;
    private char         itemsSeperator    = 0x02;
    private char         keyValueSeperator = 0x03;
    private char         lineSeperator     = '\n';
    
    private Map<Character, Character> transcodes = new HashMap<Character, Character>();
    
    public BsvContextBuilder() {
    }

    public BsvContextBuilder yamlSchema(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String actualPath = path.substring("classpath:".length());
            if (!actualPath.startsWith("/")) {
                actualPath = "/" + actualPath;
            }
            return yamlSchema(this.getClass().getResourceAsStream(actualPath));
        }
        throw new IllegalArgumentException("Invalid path: " + path);
    }
    
    public BsvContextBuilder yamlSchema(InputStream inputStream) throws IOException {
        BsvYamlSchemaParser parser = new BsvYamlSchemaParser();
        schemas.add(parser.parse(inputStream));
        return this;
    }

    public BsvContextBuilder fieldsSeperator(char fieldsSeperator) {
        this.fieldsSeperator = fieldsSeperator;
        return this;
    }
    
    public BsvContextBuilder itemsSeperator(char itemsSeperator) {
        this.itemsSeperator = itemsSeperator;
        return this;
    }
    
    public BsvContextBuilder keyValueSeperator(char keyValueSeperator) {
        this.keyValueSeperator = keyValueSeperator;
        return this;
    }
    
    public BsvContextBuilder lineSeperator(char lineSeperator) {
        this.lineSeperator = lineSeperator;
        return this;
    }
    
    
	public BsvContextBuilder transcoding(char from, char to) {
		this.transcodes.put(from, to);
		return this;
	}
	
    
    public BsvContext build() {
        return new BsvContext(schemas, fieldsSeperator, itemsSeperator, keyValueSeperator,
                lineSeperator, transcodes);
    }
}
