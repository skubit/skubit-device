
package com.skubit.shared.dto;

public class UserDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4548823909918422169L;

    private String city;

    private String contactWebsite;

    private String email;

    private String employer;

    private String fullName;

    private String logoutUrl;

    private String occuption;

    private String payoutAddress;

    private String state;

    private String streetAddress1;

    private String streetAddress2;

    private String subject;

    private String userId;

    private String userName;

    private String zipCode;

    public UserDto() {
    }

    public UserDto(String userId, String email, String userName,
            String logoutUrl) {
        this.userId = userId;
        this.email = email;
        this.logoutUrl = logoutUrl;
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public String getContactWebsite() {
        return contactWebsite;
    }

    public String getEmail() {
        return email;
    }

    public String getEmployer() {
        return employer;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public String getOccuption() {
        return occuption;
    }

    public String getPayoutAddress() {
        return payoutAddress;
    }

    public String getState() {
        return state;
    }

    public String getStreetAddress1() {
        return streetAddress1;
    }

    public String getStreetAddress2() {
        return streetAddress2;
    }

    public String getSubject() {
        return subject;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setContactWebsite(String contactWebsite) {
        this.contactWebsite = contactWebsite;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public void setOccuption(String occuption) {
        this.occuption = occuption;
    }

    public void setPayoutAddress(String payoutAddress) {
        this.payoutAddress = payoutAddress;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStreetAddress1(String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return "UserDto [city=" + city + ", contactWebsite=" + contactWebsite 
                + ", email=" + email
                + ", employer=" + employer + ", fullName=" + fullName
                + ", logoutUrl=" + logoutUrl + ", occuption=" + occuption
                + ", payoutAddress=" + payoutAddress + ", state=" + state
                + ", streetAddress1=" + streetAddress1 + ", streetAddress2="
                + streetAddress2 + ", subject=" + subject + ", userId="
                + userId + ", userName=" + userName + ", zipCode=" + zipCode
                + "]";
    }

}
