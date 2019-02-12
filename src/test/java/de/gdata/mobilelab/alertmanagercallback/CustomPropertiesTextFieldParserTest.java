package de.gdata.mobilelab.alertmanagercallback;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomPropertiesTextFieldParserTest {

    @Test
    public void constructor() {
        assertNotNull(new CustomPropertiesTextFieldParser());
    }

    @Test
    public void extractKeyValuePairsFromCustomField() throws IOException {
        // given: instance
        CustomPropertiesTextFieldParser customPropertiesTextFieldParser = new CustomPropertiesTextFieldParser();

        // when: parsing key-value-pairs from custom string
        Map<? extends String, ?> extractedPairs = customPropertiesTextFieldParser.extractKeyValuePairsFromCustomField(";key3;;key1: value1;key2=value2;;");

        // then: values should have been set
        assertEquals(3, extractedPairs.size());
        assertEquals("value1", extractedPairs.get("key1"));
        assertEquals("value2", extractedPairs.get("key2"));
        assertEquals("", extractedPairs.get("key3"));
    }

}