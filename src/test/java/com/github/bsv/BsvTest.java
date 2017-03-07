package com.github.bsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;


public class BsvTest {
    
    private static BsvContext context;

    @BeforeClass
    public static void beforeClass() throws IOException {
        context = new BsvContextBuilder()
            .yamlSchema("classpath:com/github/bsv/schema03.0.0.yaml")
            .yamlSchema("classpath:com/github/bsv/schema03.0.1.yaml")
            .yamlSchema("classpath:com/github/bsv/schema03.1.0.yaml")
            .build();
    }
    
    @Test
    public void testSingleLine() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_single.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        Schema030x data = (Schema030x) deserializer.next();
        assertEquals("ABC", data.getS());
        assertEquals("", data.getN());
        assertTrue(data.isB());
        assertEquals(12345678, data.getI());
        assertEquals(Arrays.asList("1.2", "2.4", "3.4"), data.getArray());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "4");
        matching.put("b", "d");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testBatchLine() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_batch.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals("ABC", data.getS());
        assertEquals("", data.getN());
        assertFalse(Boolean.FALSE);
        assertEquals(87654321, data.getI());
        assertEquals(Arrays.asList("1.2", "2.4", "3.4"), data.getArray());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "4");
        matching.put("b", "d");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testChinese() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_chinese.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals("A币C", data.getS());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", "四");
        matching.put("b", "地");
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
    }
    
    @Test
    public void testEmoji() throws IOException, BsvException {
        Scanner emojiData = new Scanner(this.getClass().getResourceAsStream(
                "/com/github/bsv/testEmoji.data"), "UTF-8");
        String s = emojiData.nextLine();
        String a = emojiData.nextLine();
        String b = emojiData.nextLine();
        
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_emoji.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line
        Schema030x data = (Schema030x) deserializer.next(); //third line
        assertEquals(s, data.getS());
        Map<String, String> matching = new HashMap<String, String>();
        matching.put("a", a);
        matching.put("b", b);
        assertEquals(matching, data.getMap());
        assertNull(deserializer.next());
        
        emojiData.close();
    }
    
    @Test
    public void testVariants() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_variants.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        Schema030x data2 = (Schema030x) deserializer.next(); //second line
        Schema030x data3 = (Schema030x) deserializer.next(); //third line
        assertEquals("", data2.getN());
        assertNull(data3.getN());
    }
    
    @Test(expected = BsvException.class)
    public void testInvalid() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_invalid.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        deserializer.next(); //first line
        deserializer.next(); //second line, should throw exception
    }
    
    @Test
    public void testEmptymap() throws IOException, BsvException {
        InputStream inputStream = this.getClass().getResourceAsStream(
                "/com/github/bsv/sample_emptymap.txt");

        BsvDeserializer deserializer = context.createDeserializer(inputStream);
        Schema030x data = (Schema030x) deserializer.next(); //first line
        assertEquals("ABC", data.getS());
        assertTrue(data.getMap().isEmpty());
    }
}
