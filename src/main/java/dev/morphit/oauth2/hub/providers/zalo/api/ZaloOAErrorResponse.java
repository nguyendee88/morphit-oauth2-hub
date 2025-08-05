package dev.morphit.oauth2.hub.providers.zalo.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author morphit.dee88
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZaloOAErrorResponse {

    @JsonProperty("error_name")
    private String errorName;

    @JsonProperty("error_reason")
    private String errorReason;

    @JsonProperty("ref_doc")
    private String refDoc;

    @JsonProperty("error_description")
    private String errorDescription;

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getRefDoc() {
        return refDoc;
    }

    public void setRefDoc(String refDoc) {
        this.refDoc = refDoc;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
