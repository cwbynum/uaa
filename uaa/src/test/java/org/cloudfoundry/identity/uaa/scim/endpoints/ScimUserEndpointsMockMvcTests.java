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
package org.cloudfoundry.identity.uaa.scim.endpoints;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cloudfoundry.identity.uaa.scim.ScimUser;
import org.cloudfoundry.identity.uaa.scim.ScimUserProvisioning;
import org.cloudfoundry.identity.uaa.test.DefaultIntegrationTestConfig;
import org.cloudfoundry.identity.uaa.test.IntegrationTestContextLoader;
import org.cloudfoundry.identity.uaa.test.TestClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.googlecode.flyway.core.Flyway;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = DefaultIntegrationTestConfig.class, loader = IntegrationTestContextLoader.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class ScimUserEndpointsMockMvcTests {

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @Autowired Flyway flyway;

    private MockMvc mockMvc;
    private String scimToken;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();

        TestClient testClient = new TestClient(mockMvc);
        String adminToken = testClient.getOAuthAccessToken("admin", "adminsecret", "client_credentials",
                        "clients.read clients.write clients.secret");
        createScimClient(adminToken);
        scimToken = testClient.getOAuthAccessToken("scim", "scimsecret", "client_credentials",
                        "scim.read scim.write password.write");
    }

    @After
    public void tearDown() throws Exception {
        flyway.clean();
    }

    @Test
    public void testCreateUser() throws Exception {
        ScimUser user = new ScimUser();
        user.setUserName("JOE");
        user.setName(new ScimUser.Name("Joe", "User"));
        user.addEmail("joe@blah.com");

        byte[] requestBody = new ObjectMapper().writeValueAsBytes(user);
        MockHttpServletRequestBuilder post = post("/Users")
                        .header("Authorization", "Bearer " + scimToken)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody);

        mockMvc.perform(post)
                        .andExpect(status().isCreated())
                        .andExpect(header().string("ETag", "\"0\""))
                        .andExpect(jsonPath("$.userName").value("JOE"))
                        .andExpect(jsonPath("$.emails[0].value").value("joe@blah.com"))
                        .andExpect(jsonPath("$.name.familyName").value("User"))
                        .andExpect(jsonPath("$.name.givenName").value("Joe"));
    }

    @Test
    public void testGetUser() throws Exception {
        ScimUserProvisioning usersRepository = webApplicationContext.getBean(ScimUserProvisioning.class);
        ScimUser joel = new ScimUser(null, "jdsa", "Joel", "D'sa");
        joel.addEmail("jdsa@vmware.com");
        joel = usersRepository.createUser(joel, "password");

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/Users/" + joel.getId())
                        .header("Authorization", "Bearer " + scimToken)
                        .accept(APPLICATION_JSON);

        mockMvc.perform(get)
                        .andExpect(status().isOk())
                        .andExpect(header().string("ETag", "\"0\""))
                        .andExpect(jsonPath("$.userName").value("jdsa"))
                        .andExpect(jsonPath("$.emails[0].value").value("jdsa@vmware.com"))
                        .andExpect(jsonPath("$.name.familyName").value("D'sa"))
                        .andExpect(jsonPath("$.name.givenName").value("Joel"));
    }

    private void createScimClient(String adminAccessToken) throws Exception {
        MockHttpServletRequestBuilder createClientPost = post("/oauth/clients")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"scope\":[\"uaa.none\"],\"client_id\":\"scim\",\"client_secret\":\"scimsecret\",\"resource_ids\":[\"oauth\"],\"authorized_grant_types\":[\"client_credentials\"],\"authorities\":[\"password.write\",\"scim.write\",\"scim.read\",\"oauth.approvals\"]}")
                        .accept(APPLICATION_JSON);
        mockMvc.perform(createClientPost).andExpect(status().isCreated());
    }
}
