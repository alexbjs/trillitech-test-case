package com.crescendocollective.email;

import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;

import javax.jcr.Node;
import org.apache.commons.lang.StringUtils;

public class EmailForm extends RenderingModelImpl<ConfiguredTemplateDefinition> {

    private String firstName;
    private String lastName;
    private String email;
    private String statusMessage;
    private boolean addToMailingList = false;

    public EmailForm(Node content, ConfiguredTemplateDefinition definition, RenderingModel<?> parent) {
        super(content, definition, parent);
        firstName = "";
        lastName = "";
        email = "";
        statusMessage = "";
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isFirstNameValid() {
        return !StringUtils.isEmpty(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAddToMailingList() {
        return addToMailingList;
    }

    public void setAddToMailingList(boolean addToMailingList) {
        this.addToMailingList = addToMailingList;
    }

    public boolean isLastNameValid() {
        return !StringUtils.isEmpty(lastName);
    }

    public boolean isEmailValid() {
        return email != null && email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21"
            + "\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:"
            + "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:"
            + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?"
            + "[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a"
            + "\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    }

    public void submit(String recipient, String username, String password,
                              String smtpAuth, String smtpStartlsOn, String smtpHost, String smtpPort) {
        if (!this.isFirstNameValid() || !this.isLastNameValid()) {
            statusMessage = "Invalid name or lastName!";
            return;
        } else if (!this.isEmailValid()) {
            statusMessage = "Invalid email!";
            return;
        }
        statusMessage = "You've been added to the mailing list";
        String notification = "Please contact us";
        if (addToMailingList) {
            notification += " and add " + email + " to the mailing list.";
        }
        notification += ".\n Best wishes, \n" + firstName + " " + lastName;

        EmailService emailService = new EmailService(username, password, smtpAuth, smtpStartlsOn, smtpHost, smtpPort);
        emailService.sendEmail(email, !StringUtils.isEmpty(recipient) ? "proxy8925@gmail.com" : recipient, notification);
    }

}
