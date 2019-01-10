package de.gdata.mobilelab.alertmanagercallback;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class AlertManagerPayloadTest {

    @Test
    public void constructor() {
        assertNotNull(new AlertManagerPayload());

        Map<String, Object> map = mock(Map.class);

        assertEquals(map, new AlertManagerPayload(map, null, null, null, null).getLabels());
        assertEquals(map, new AlertManagerPayload(null, map, null, null, null).getAnnotations());
        assertEquals("foo", new AlertManagerPayload(null, null, "foo", null, null).getGeneratorURL());
        assertEquals("foo", new AlertManagerPayload(null, null, null, "foo", null).getStartsAt());
        assertEquals("foo", new AlertManagerPayload(null, null, null, null, "foo").getEndsAt());
    }

    @Test
    public void setterAndGetter() {

        Map<String, Object> map1 = mock(Map.class);

        Map<String, Object> map2 = mock(Map.class);

        AlertManagerPayload alertManagerPayload = new AlertManagerPayload();
        alertManagerPayload.setGeneratorURL("url");
        alertManagerPayload.setEndsAt("endsAt");
        alertManagerPayload.setStartsAt("startsAt");
        alertManagerPayload.setAnnotations(map1);
        alertManagerPayload.setLabels(map2);

        assertEquals("url", alertManagerPayload.getGeneratorURL());
        assertEquals("endsAt", alertManagerPayload.getEndsAt());
        assertEquals("startsAt", alertManagerPayload.getStartsAt());
        assertEquals(map1, alertManagerPayload.getAnnotations());
        assertEquals(map2, alertManagerPayload.getLabels());
    }

}