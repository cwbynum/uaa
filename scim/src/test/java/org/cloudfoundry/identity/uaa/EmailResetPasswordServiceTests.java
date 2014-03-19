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

import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.cloudfoundry.identity.uaa.scim.ScimUserProvisioning;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import java.util.Arrays;

public class EmailResetPasswordServiceTests {

    private SimpleSmtpServer simpleSmtpServer;
    private EmailResetPasswordService emailResetPasswordService;
    private ScimUserProvisioning scimUserProvisioning;

    @Before
    public void setUp() throws Exception {
        int port = 2525;
        simpleSmtpServer = SimpleSmtpServer.start(port);
        scimUserProvisioning = Mockito.mock(ScimUserProvisioning.class);
        emailResetPasswordService = new EmailResetPasswordService(scimUserProvisioning, "localhost", port, "");
    }

    @After
    public void tearDown() throws Exception {
        simpleSmtpServer.stop();
    }

    @Test
    public void testWhenTheEmailExists_ResetPassword() throws Exception {
        ScimUser user = new ScimUser(null, null, null, null);
        user.addEmail("user@example.com");
        Mockito.when(scimUserProvisioning.query("email eq 'user@example.com' or userName eq 'user@example.com'"))
                .thenReturn(Arrays.asList(user));

        emailResetPasswordService.resetPassword("user@example.com");

        Assert.assertEquals(1, simpleSmtpServer.getReceivedEmailSize());
        SmtpMessage message = (SmtpMessage) simpleSmtpServer.getReceivedEmail().next();
        Assert.assertEquals("user@example.com", message.getHeaderValue("To"));
        Assert.assertEquals("This is a placeholder email.  We cannot support resetting of passwords just yet.  Sorry for the ruse.", message.getBody());
    }

    @Test
    public void testWhenTheUserExists_ResetPassword() throws Exception {
        ScimUser user = new ScimUser(null, "userman", null, null);
        user.addEmail("user@example.com");
        Mockito.when(scimUserProvisioning.query("email eq 'userman' or userName eq 'userman'"))
                .thenReturn(Arrays.asList(user));

        emailResetPasswordService.resetPassword("userman");

        Assert.assertEquals(1, simpleSmtpServer.getReceivedEmailSize());
        SmtpMessage message = (SmtpMessage) simpleSmtpServer.getReceivedEmail().next();
        Assert.assertEquals("user@example.com", message.getHeaderValue("To"));
        Assert.assertEquals("This is a placeholder email.  We cannot support resetting of passwords just yet.  Sorry for the ruse.", message.getBody());
    }

    @Test
    public void testWhenTheUserDoesNotExist_ResetPassword() throws Exception {
        Mockito.when(scimUserProvisioning.query("email eq 'user@example.com' or userName eq 'user@example.com'"))
                .thenReturn(Arrays.<ScimUser>asList());

        emailResetPasswordService.resetPassword("user@example.com");

        Assert.assertEquals(0, simpleSmtpServer.getReceivedEmailSize());
    }
}
