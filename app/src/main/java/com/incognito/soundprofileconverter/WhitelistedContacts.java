package com.incognito.soundprofileconverter;

public class WhitelistedContacts {

    private int id;
    private String contactName;
    private String contactPhoneNumber;

    public WhitelistedContacts(String contactName, String contactPhoneNumber) {
        this.contactName = contactName;
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

}
