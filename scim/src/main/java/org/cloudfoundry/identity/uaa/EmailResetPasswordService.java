/*******************************************************************************
 *     Cloud Foundry 
 *     Copyright (c) [2009-2014] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/
package org.cloudfoundry.identity.uaa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.cloudfoundry.identity.uaa.scim.ScimUserProvisioning;

import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class EmailResetPasswordService implements ResetPasswordService {

    private final Log logger = LogFactory.getLog(getClass());

    private final int smtpPort;
    private final ScimUserProvisioning scimUserProvisioning;
    private final String smtpHost;
    private final String smtpUser;
    private final String smtpPassword;

    public EmailResetPasswordService(ScimUserProvisioning scimUserProvisioning, String smtpHost, int smtpPort, String smtpUser, String smtpPassword) {
        this.scimUserProvisioning = scimUserProvisioning;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
    }

    @Override
    public void resetPassword(String emailOrUsername) {
        List<ScimUser> results = scimUserProvisioning.query("email eq '" + emailOrUsername + "' or userName eq '" + emailOrUsername + "'");
        if (!results.isEmpty()) {
            ScimUser user = results.get(0);
            MimeMessage message = new MimeMessage(getSession());
            try {
                message.addRecipients(Message.RecipientType.TO, user.getPrimaryEmail());
                message.setSubject("Password Reset Instructions");
                message.setText("This is a placeholder email.  We cannot support resetting of passwords just yet.  Sorry for the ruse.");
                Transport.send(message);
            } catch (MessagingException e) {
                logger.error("Exception raised while sending message to " + emailOrUsername, e);
            }
        }
    }

    private Session getSession() {
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.smtp.host", smtpHost);
        mailProperties.setProperty("mail.smtp.port", "" + smtpPort);
        mailProperties.setProperty("mail.smtp.user", smtpUser);
        return Session.getDefaultInstance(mailProperties, new StaticAuthenticator());
    }

    private class StaticAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUser, smtpPassword);
        }
    }
}
