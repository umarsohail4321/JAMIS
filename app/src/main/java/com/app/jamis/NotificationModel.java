package com.app.jamis;

public class NotificationModel {
    private String jobCategory;
    private String employeeID;
    private String employeeName;
    private String status;
    private String message;
    private long timestamp;
    public NotificationModel() {
    }


    public NotificationModel(  String employeeID,String employeeName,String jobCategory, String status, String message) {
        this.jobCategory = jobCategory;
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }
    public String getemployeeID() {
        return employeeID;
    }

    public void setemployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getemployeeName() {
        return employeeName;
    }

    public void setemployeeName(String employeeName) {
        this.employeeName = employeeName;
    }


    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
