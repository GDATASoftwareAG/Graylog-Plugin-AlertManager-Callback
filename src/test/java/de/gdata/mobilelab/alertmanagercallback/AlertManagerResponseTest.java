package de.gdata.mobilelab.alertmanagercallback;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AlertManagerResponseTest {

    @Test
    public void constructor() {
        assertNotNull(new AlertManagerResponse());
        assertNotNull(new AlertManagerResponse(null));
        assertEquals("status", new AlertManagerResponse("status").getStatus());
    }

    @Test
    public void setterAndGetter() {
        AlertManagerResponse alertManagerResponse = new AlertManagerResponse();
        alertManagerResponse.setStatus("status");
        assertEquals("status", alertManagerResponse.getStatus());
    }

}