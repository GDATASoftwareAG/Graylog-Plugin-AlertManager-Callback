package de.gdata.mobilelab.alertmanagercallback;

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
class AlertManagerResponse {

    static final String STATUS_SUCCESS = "success";

    private String status;

}
