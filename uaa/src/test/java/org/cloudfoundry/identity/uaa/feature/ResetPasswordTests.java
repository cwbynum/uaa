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
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DefaultFeatureTestConfig.class, loader = IntegrationTestContextLoader.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ResetPasswordTests {

    @Autowired
    WebDriver webDriver;

    @Test
    public void testPage() throws Exception {
        webDriver.get("http://localhost:9090/uaa/login");
        Assert.assertEquals("UAA Login | Cloud Foundry", webDriver.getTitle());
    }
}
