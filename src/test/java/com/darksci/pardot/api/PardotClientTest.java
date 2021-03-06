/**
 * Copyright 2017, 2018, 2019, 2020 Stephen Powis https://github.com/Crim/pardot-java-client
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.darksci.pardot.api;

import com.darksci.pardot.api.request.login.LoginRequest;
import com.darksci.pardot.api.request.tag.TagReadRequest;
import com.darksci.pardot.api.response.login.LoginResponse;
import com.darksci.pardot.api.response.tag.Tag;
import com.darksci.pardot.api.rest.RestClient;
import com.darksci.pardot.api.rest.RestResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import util.TestHelper;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit testing over PardotClient.
 */
public class PardotClientTest {
    // By default expect no exception.
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    // Dependencies
    private Configuration apiConfig;
    private RestClient mockRestClient;

    // Instance under test
    private PardotClient pardotClient;

    @Before
    public void before() {
        // Create configuration
        final String userEmail = "user@example.com";
        final String userPassword = "NotARealPassword";
        final String userKey = "NotARealUserKey";
        apiConfig = new Configuration(userEmail, userPassword, userKey);

        // Create mock RestClient
        mockRestClient = mock(RestClient.class);

        // Create instance using mock dependencies.
        pardotClient = new PardotClient(apiConfig, mockRestClient);

    }

    /**
     * Smoke test over login requests.
     */
    @Test
    public void smokeTestDirectLoginRequest() {
        // Construct request.
        final LoginRequest loginRequest = new LoginRequest()
            .withEmail(apiConfig.getEmail())
            .withPassword(apiConfig.getPassword());

        // Mock response
        when(mockRestClient.submitRequest(isA(LoginRequest.class)))
            .thenReturn(createRestResponseFromFile("login.xml", 200));

        // Call method under test
        final LoginResponse response = pardotClient.login(loginRequest);

        // Validate
        assertNotNull(response);
        assertEquals("DontWorryIDidNotCheckInARealHash", response.getApiKey());

        // Verify mock interactions
        verify(mockRestClient, times(1))
            .submitRequest(any(LoginRequest.class));
        verifyNoMoreRestClientInteractions();
    }

    /**
     * Verifies the behavior of PardotClient when the library has not yet authenticated to Pardot's API.
     *
     * Expected behavior is the library internally detects that no valid session exists
     * and attempts to authenticate automatically, and then makes the original request.
     *
     * We will execute a request to retrieve a tag by id.  This should trigger the library
     * to first attempt to authenticate.  After that is successful, it should execute our original request.
     */
    @Test
    public void testIndirectLogin() {
        // Sanity test
        assertNull("ApiKey should start as null prior to login", apiConfig.getApiKey());

        // Construct request to query a tag
        // This exact request isn't really relevant. Just that it will trigger
        // the library to authenticate automatically.
        final TagReadRequest tagReadRequest = new TagReadRequest()
            .selectById(1L);

        // Mock responses from RestClient/Api Server.
        when(mockRestClient.submitRequest(isA(LoginRequest.class)))
            .thenReturn(createRestResponseFromFile("login.xml", 200));
        when(mockRestClient.submitRequest(isA(TagReadRequest.class)))
            .thenReturn(createRestResponseFromFile("tagRead.xml", 200));

        // Call method under test
        final Tag response = pardotClient.tagRead(tagReadRequest);

        // Validate response for tag
        assertNotNull(response);
        assertEquals(1L, (long) response.getId());
        assertEquals("Standard Tag", response.getName());

        // Validate we updated our ApiConfig based on the login.
        assertNotNull("ApiKey should no longer be null", apiConfig.getApiKey());
        assertEquals("DontWorryIDidNotCheckInARealHash", apiConfig.getApiKey());

        // Verify mock interactions
        verify(mockRestClient, times(1))
            .submitRequest(isA(LoginRequest.class));
        verify(mockRestClient, times(1))
            .submitRequest(isA(TagReadRequest.class));
        verifyNoMoreRestClientInteractions();
    }

    /**
     * Verifies the behavior of PardotClient when the login session times out.
     *
     * Expected behavior is the library internally captures the invalid session error response
     * and attempts to re-authenticate automatically, and then replays the original request.
     *
     * We will execute a request to retrieve a tag by id.  This should trigger the library
     * to first attempt to authenticate.  After that is successful, it should execute our original request.
     */
    @Test
    public void testReAuthenticationOnSessionTimeout() {
        // Lets set a dummy Authentication Key to simulate already having a valid session
        apiConfig.setApiKey("OriginalDummyKey");

        // Construct request to query a tag
        // This exact request isn't really relevant. Just that it will trigger
        // the library to authenticate automatically.
        final TagReadRequest tagReadRequest = new TagReadRequest()
            .selectById(1L);

        // Mock responses from RestClient/Api Server.
        when(mockRestClient.submitRequest(isA(LoginRequest.class)))
            .thenReturn(createRestResponseFromFile("login.xml", 200));

        when(mockRestClient.submitRequest(isA(TagReadRequest.class)))
            .thenReturn(
                // First call should return an invalid API key response.
                createRestResponseFromFile("errorInvalidApiKey.xml", 200),

                // Second call should return the real tag read response.
                createRestResponseFromFile("tagRead.xml", 200)
            );

        // Call method under test
        final Tag response = pardotClient.tagRead(tagReadRequest);

        // Validate response for tag
        assertNotNull(response);
        assertEquals(1L, (long) response.getId());
        assertEquals("Standard Tag", response.getName());

        // Validate we updated our ApiConfig based on the login.
        assertNotNull("ApiKey should no longer be null", apiConfig.getApiKey());
        assertEquals("DontWorryIDidNotCheckInARealHash", apiConfig.getApiKey());

        // Verify mock interactions
        verify(mockRestClient, times(1))
            .submitRequest(isA(LoginRequest.class));
        verify(mockRestClient, times(2))
            .submitRequest(isA(TagReadRequest.class));
        verifyNoMoreRestClientInteractions();
    }

    /**
     * Verifies the behavior of PardotClient when the login session times out, and when we
     * attempt to renew the session we get invalid credentials.
     *
     * First we simulate having a valid ApiUserKey/Session.
     * We request reading a tag, and have the API server return an invalid session result.
     * The library should automatically attempt to renew the session.  We mock
     * the servers response to the login with an invalid credentials response.
     *
     * We expect the library to throw a LoginFailedException.
     */
    @Test
    public void testReAuthenticationOnSessionTimeout_triggersInvalidCredentials() {
        // Lets set a dummy Authentication Key to simulate already having a valid session
        apiConfig.setApiKey("OriginalDummyKey");

        // Construct request to query a tag
        // This exact request isn't really relevant. Just that it will trigger
        // the library to authenticate automatically.
        final TagReadRequest tagReadRequest = new TagReadRequest()
            .selectById(1L);

        // Mock responses from RestClient/Api Server.
        // When we request for a tag read, we should get an invalid API key response.
        when(mockRestClient.submitRequest(isA(TagReadRequest.class)))
            .thenReturn(
                // First call should return an invalid API key response.
                createRestResponseFromFile("errorInvalidApiKey.xml", 200)
            );

        // When it attempts to renew the session, we should get an invalid credentials response.
        when(mockRestClient.submitRequest(isA(LoginRequest.class)))
            .thenReturn(createRestResponseFromFile("errorLoginFailed.xml", 200));


        // Call method under test, this should throw an exception
        expectedException.expect(LoginFailedException.class);
        pardotClient.tagRead(tagReadRequest);
    }

    private RestResponse createRestResponseFromFile(final String filename, int httpCode) {
        try {
            return new RestResponse(
                TestHelper.readFile("mockResponses/" + filename),
                httpCode
            );
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void verifyNoMoreRestClientInteractions() {
        verify(mockRestClient, times(1))
            .init(apiConfig);
        verifyNoMoreInteractions(mockRestClient);
    }
}
