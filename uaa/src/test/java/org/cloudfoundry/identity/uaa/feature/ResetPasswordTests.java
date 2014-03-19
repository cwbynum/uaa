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
package org.cloudfoundry.identity.uaa.feature;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import org.cloudfoundry.identity.uaa.test.DefaultFeatureTestConfig;
import org.cloudfoundry.identity.uaa.test.IntegrationTestContextLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DefaultFeatureTestConfig.class, loader = IntegrationTestContextLoader.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ResetPasswordTests {

    @Autowired
    WebDriver webDriver;

    @Autowired
    SimpleSmtpServer simpleSmtpServer;

    @Test
    public void requestingAPasswordReset() throws Exception {
        webDriver.get("http://localhost:9090/uaa/login");
        Assert.assertEquals("UAA Login | Cloud Foundry", webDriver.getTitle());

        webDriver.findElement(By.linkText("Forgot Password?")).click();
        Assert.assertEquals("UAA Reset Password | Cloud Foundry", webDriver.getTitle());

        webDriver.findElement(By.name("email")).sendKeys("user@example.com");
        webDriver.findElement(By.xpath("//button[contains(text(),'Reset Password')]")).click();

        Assert.assertEquals(1, simpleSmtpServer.getReceivedEmailSize());
        SmtpMessage message = (SmtpMessage) simpleSmtpServer.getReceivedEmail().next();
        Assert.assertEquals("user@example.com", message.getHeaderValue("To"));
        Assert.assertEquals("This is a placeholder email.  We cannot support resetting of passwords just yet.  Sorry for the ruse.", message.getBody());

        Assert.assertEquals("An email has been sent with password reset instructions.", webDriver.findElement(By.cssSelector(".flash")).getText());
        Assert.assertEquals("UAA Reset Password | Cloud Foundry", webDriver.getTitle());
    }
}
