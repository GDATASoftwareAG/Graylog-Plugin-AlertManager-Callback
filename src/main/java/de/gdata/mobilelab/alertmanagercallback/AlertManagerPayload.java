package de.gdata.mobilelab.alertmanagercallback;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class AlertManagerPayload {

    private Map<String, Object> labels;
    private Map<String, Object> annotations;
    private String generatorURL;
    private String startsAt;
    private String endsAt;

}
