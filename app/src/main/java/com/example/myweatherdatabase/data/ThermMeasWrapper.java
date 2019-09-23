package com.example.myweatherdatabase.data;

import java.util.List;

public class ThermMeasWrapper {

    private List<ThermMeasurement> measurements;
    private boolean hasError;
    private int resultCode;
    private String statusMessage;

    public ThermMeasWrapper(List<ThermMeasurement> measurements,
                            boolean hasError,
                            int errorType,
                            String statusMessage) {
        this.measurements = measurements;
        this.hasError = hasError;
        this.resultCode = errorType;
        this.statusMessage = statusMessage;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<ThermMeasurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<ThermMeasurement> measurements) {
        this.measurements = measurements;
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
