package de.gdata.mobilelab.alertmanagercallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This parser can parse key-value-pairs from a text field string.
 * It uses {@link #KEY_VALUE_PAIR_SEPARATOR} for separating each key-value-pair.
 * The keys and related values will be parsed using {@link Properties#load(InputStream)}.
 */
class CustomPropertiesTextFieldParser {

    /** The separator used in text field to split between key-value-pairs. */
    static final String KEY_VALUE_PAIR_SEPARATOR = ";";

    /** The separator used by {@link Properties#load(InputStream)} for key-value-pair separation. */
    private static final String PROPERTIES_KEY_VALUE_PAIR_SEPARATOR = "\n";

    /**
     * Parses the text field value with custom key-value-pairs into a map.<br>
     * It uses the {@link #KEY_VALUE_PAIR_SEPARATOR} for the line separation. This separation is required
     * for loading the key-value-pairs using {@link Properties#load(InputStream)}.
     *
     * @param textFieldValue the text field value
     * @return the map with key-value-pairs from text field
     * @throws IOException if parsing the key-value-pairs fails
     */
    Map<? extends String, ?> extractKeyValuePairsFromCustomField(String textFieldValue) throws IOException {
        Map<String, Object> extractedPairs = new HashMap<>();

        if (textFieldValue != null && !"".equals(textFieldValue)) {
            final String preparedTextFieldValue = textFieldValue.replaceAll(KEY_VALUE_PAIR_SEPARATOR, PROPERTIES_KEY_VALUE_PAIR_SEPARATOR);
            Properties properties = new Properties();
            InputStream stringInputStream = new ByteArrayInputStream(preparedTextFieldValue.getBytes(StandardCharsets.UTF_8));
            properties.load(stringInputStream);
            properties.forEach((key, value) -> extractedPairs.put((String) key, value));
        }

        return extractedPairs;
    }

}
