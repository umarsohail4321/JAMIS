package com.app.jamis;

public class Job {
    private String jobTitle;
    private String companyName;
    private String designation2;
    private String skills2;
    private int imageResId;
    private String country;
    private String city;
    private String jobId;

    public Job(String jobTitle, String companyName, String designation2, String skills2, int imageResId, String country, String city) {
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.designation2 = designation2;
        this.skills2 = skills2;
        this.imageResId = imageResId;
        this.country = country;
        this.city = city;
    }

    public Job() {
    }

    // Getters
    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDesignation2() { return designation2;}
    public String getskills2() { return skills2;}

    public int getImageResId() {
        return imageResId;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setDesignation2(String designation2) {
        this.designation2 = designation2;
    }

    public String getSkills2() {
        return skills2;
    }

    public void setSkills2(String skills2) {
        this.skills2 = skills2;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
