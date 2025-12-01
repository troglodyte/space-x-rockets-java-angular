package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SpaceXRocketsApiTest {

    private RestTemplate restTemplate;
    private SpaceXRocketsApi api;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        api = new SpaceXRocketsApi(restTemplate);
    }

    @Test
    void getApiResponse_withClass_returnsValue() {
        when(restTemplate.getForObject(eq("http://x"), eq(String.class))).thenReturn("ok");

        String res = api.getApiResponse("http://x", String.class);
        assertEquals("ok", res);
    }

    @Test
    void getApiResponse_withClass_returnsNullOnException() {
        when(restTemplate.getForObject(eq("http://x"), eq(String.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("boom"));

        String res = api.getApiResponse("http://x", String.class);
        assertNull(res, "Should return null when RestTemplate throws RestClientException");
    }

    @Test
    void getSpaceXData_buildsUrlFromBase() throws Exception {
        setBaseUrl();
        when(restTemplate.getForObject(eq("https://base/endpoint"), eq(String.class)))
                .thenReturn("data");

        // call the protected method via same-package access
        String result = invokeGetSpaceXData();
        assertEquals("data", result);
    }

    @Test
    void rocketsAndLaunchesEndpoints_areCorrect() throws Exception {
        setBaseUrl();
        when(restTemplate.getForObject(eq("https://base/v4/rockets"), eq(String.class))).thenReturn("R");
        when(restTemplate.getForObject(eq("https://base/v4/launches"), eq(String.class))).thenReturn("L");

        assertEquals("R", api.getRocketsData());
        assertEquals("L", api.getAllLaunchesData());
    }

    private void setBaseUrl() throws Exception {
        var field = SpaceXRocketsApi.class.getDeclaredField("spaceXApiUrl");
        field.setAccessible(true);
        field.set(api, "https://base");
    }

    private String invokeGetSpaceXData() throws Exception {
        var method = SpaceXRocketsApi.class.getDeclaredMethod("getSpaceXData", String.class);
        method.setAccessible(true);
        return (String) method.invoke(api, "endpoint");
    }
}
