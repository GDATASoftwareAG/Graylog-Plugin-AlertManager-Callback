package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlertManagerAlarmCallbackTest {

    @Test
    public void initialize() throws AlarmCallbackConfigurationException, NoSuchFieldException, IllegalAccessException {
        // given: A configuration mock
        Configuration configuration = mock(Configuration.class);

        // and: an instance
        AlertManagerAlarmCallback alertManagerAlarmCallback = new AlertManagerAlarmCallback();

        // when: calling initialize with given configuration
        alertManagerAlarmCallback.initialize(configuration);

        // then: alertManagerAlarmCallback should have been correctly initialized
        // Check that alertManagerPostRequestSender is not null
        Field field = AlertManagerAlarmCallback.class.getDeclaredField("alertManagerPostRequestSender");
        field.setAccessible(true);
        assertNotNull(field.get(alertManagerAlarmCallback));

        // and: Configuration has been set
        field = AlertManagerAlarmCallback.class.getDeclaredField("configuration");
        field.setAccessible(true);
        assertEquals(configuration, field.get(alertManagerAlarmCallback));
    }

    @Test
    public void getRequestedConfiguration() {
        // given: An instance
        AlertManagerAlarmCallback alertManagerAlarmCallback = new AlertManagerAlarmCallback();

        // when: calling getRequestedConfiguration
        ConfigurationRequest configurationRequest = alertManagerAlarmCallback.getRequestedConfiguration();

        // then: text fields have been set
        assertEquals(4, configurationRequest.getFields().size());

        ConfigurationField field = configurationRequest.getField("alertmanager_api_url");
        assertNotNull(field);
        assertEquals(TextField.FIELD_TYPE, field.getFieldType());
        assertEquals("AlertManager API URL", field.getHumanName());
        assertEquals("http://localhost:9093/api/v1/alerts", field.getDefaultValue());
        assertEquals("This callback sends a POST-Request to an AlertManager API. It converts the information into a format which is readable by the AlertManager.", field.getDescription());
        assertEquals(ConfigurationField.Optional.NOT_OPTIONAL, field.isOptional());


        field = configurationRequest.getField("alertmanager_alert_name");
        assertNotNull(field);
        assertEquals(TextField.FIELD_TYPE, field.getFieldType());
        assertEquals("AlertManager Alert Name", field.getHumanName());
        assertEquals("TestAlert", field.getDefaultValue());
        assertEquals("The name for the specific AlertManager alert (will be transmitted as 'alertname'-label).", field.getDescription());
        assertEquals(ConfigurationField.Optional.NOT_OPTIONAL, field.isOptional());

        field = configurationRequest.getField("alertmanager_custom_labels");
        assertNotNull(field);
        assertEquals(TextField.FIELD_TYPE, field.getFieldType());
        assertEquals("Custom AlertManager labels", field.getHumanName());
        assertEquals("", field.getDefaultValue());
        assertEquals("The custom AlertManager label key-value-pairs separated by '" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "' to set for each alert. Please use the following notation: 'label1=value1" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "label2=value2'", field.getDescription());
        assertEquals(ConfigurationField.Optional.OPTIONAL, field.isOptional());

        field = configurationRequest.getField("alertmanager_custom_annotations");
        assertNotNull(field);
        assertEquals(TextField.FIELD_TYPE, field.getFieldType());
        assertEquals("Custom AlertManager annotations", field.getHumanName());
        assertEquals("", field.getDefaultValue());
        assertEquals("The custom AlertManager annotation key-value-pairs separated by '" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "' to set for each alert. Please use the following notation: 'annotation1=value1" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "annotation2=value2'", field.getDescription());
        assertEquals(ConfigurationField.Optional.OPTIONAL, field.isOptional());
    }

    @Test
    public void getName() {
        // expect: Name will be returned correctly
        assertEquals(new AlertManagerAlarmCallback().getName(), "AlertManager Callback");
    }

    @Test
    public void getAttributes() throws AlarmCallbackConfigurationException {
        // given: A configuration mock
        Configuration configuration = mock(Configuration.class);
        Map<String, Object> map = mock(Map.class);
        when(configuration.getSource()).thenReturn(map);

        // and: initialized instance
        AlertManagerAlarmCallback alertManagerAlarmCallback = new AlertManagerAlarmCallback();
        alertManagerAlarmCallback.initialize(configuration);

        // expect: source will be successfully returned
        assertEquals(map, alertManagerAlarmCallback.getAttributes());
    }

    @Test
    public void checkConfiguration() throws AlarmCallbackConfigurationException, ConfigurationException {
        // given: A configuration mock
        Configuration configuration = mock(Configuration.class);

        // and: instance with configuration set
        AlertManagerAlarmCallback alertManagerAlarmCallback = new AlertManagerAlarmCallback();
        alertManagerAlarmCallback.initialize(configuration);

        // Check null as alertmanager_api_url
        when(configuration.getString("alertmanager_api_url")).thenReturn(null);
        try {
            alertManagerAlarmCallback.checkConfiguration();
            fail("The value 'null' as alertmanager_api_url should not be allowed.");
        } catch (ConfigurationException e) {
            // Success
        }

        // Check with invalid URI
        when(configuration.getString("alertmanager_api_url")).thenReturn("https:\\abcd");
        try {
            alertManagerAlarmCallback.checkConfiguration();
            fail("An invalid URI as alertmanager_api_url should not be allowed.");
        } catch (ConfigurationException e) {
            // Success
        }

        // Check with valid URI
        when(configuration.getString("alertmanager_api_url")).thenReturn("http://localhost:9093/api/v1/alerts");
        alertManagerAlarmCallback.checkConfiguration();
    }
}