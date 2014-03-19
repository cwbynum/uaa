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

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class EmailResetPasswordService implements ResetPasswordService {

    private final int smtpPort;

    public EmailResetPasswordService(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    @Override
    public void resetPassword(String email) {
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.smtp.port", "" + smtpPort);
        Session session = Session.getDefaultInstance(mailProperties);
        MimeMessage message = new MimeMessage(session);
        try {
            message.addRecipients(Message.RecipientType.TO, email);
            message.setSubject("Password Reset Instructions");
            message.setText("This is a placeholder email.  We cannot support resetting of passwords just yet.  Sorry for the ruse.");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
