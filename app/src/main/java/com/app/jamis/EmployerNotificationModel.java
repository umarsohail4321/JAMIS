package com.app.jamis;

public class EmployerNotificationModel {
    private String employeeName;
    private String jobId;
    private String employeeID;
    private String employerID;
    private String jobCategory;
    private String message;
    private long timestamp;
    private String status;

    // Empty constructor required for Firebase
    public EmployerNotificationModel() {
    }

    public EmployerNotificationModel(  String employeeID, String employerID,String employeeName, String jobCategory,String message, long timestamp) {
        this.employeeID = employeeID;
        this.message = message;
        this.employerID = employerID;
        this.employeeName = employeeName;
        this.jobCategory = jobCategory;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters and setters
    public String getEmployeeID() {
        return employeeID;
    }
    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployerID() {
        return employerID;
    }

    public void setEmployerID(String employerID) {
        this.employerID = employerID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }
    public String getMessage() {
        return message; // Getter for message
    }

    public void setMessage(String message) {
        this.message = message; // Setter for message
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}



