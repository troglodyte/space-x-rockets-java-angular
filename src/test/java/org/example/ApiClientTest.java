package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiClientTest {

    private RestTemplate restTemplate;
    private SpaceXRocketsApi spaceXRocketsApi;
    private final String baseUrl = "https://api.spacexdata.com/v4";

    @BeforeEach
    void setUp() throws Exception {
        // Create a mocked RestTemplate and a real ApiClient using that mock
        restTemplate = Mockito.mock(RestTemplate.class);
        spaceXRocketsApi = new SpaceXRocketsApi(restTemplate);

        // Inject the SpaceX base URL into the ApiClient (normally set by @Value in Spring)
        Field urlField = SpaceXRocketsApi.class.getDeclaredField("spaceXApiUrl");
        urlField.setAccessible(true);
        urlField.set(spaceXRocketsApi, baseUrl);
    }

    /**
     * Test to verify that the getSpaceXData method correctly returns data
     * when valid JSON is fetched from the SpaceX API.
     */
    @Test
    public void testGetSpaceXData_SuccessfulRequest() {
        // Setup
        String mockResponse = "{\"name\": \"Falcon 9\"}";
        String mockEndpoint = "rockets";
        String mockUrl = baseUrl + "/" + mockEndpoint;

        Mockito.when(restTemplate.getForObject(mockUrl, String.class)).thenReturn(mockResponse);

        // Execute
        String result = spaceXRocketsApi.getSpaceXData(mockEndpoint);

        // Verify
        assertEquals(mockResponse, result);
    }

    /**
     * Test to verify that getSpaceXData returns a formatted error message
     * when the SpaceX API call fails with an exception.
     */
    @Test
    public void testGetSpaceXData_ErrorHandling() {
        // Setup
        String mockEndpoint = "rockets";
        String mockUrl = baseUrl + "/" + mockEndpoint;

        Mockito.when(restTemplate.getForObject(mockUrl, String.class))
                .thenThrow(new RestClientException("API call failed"));

        // Execute
        String result = spaceXRocketsApi.getSpaceXData(mockEndpoint);

        // Verify
        assertTrue(result.contains("Error fetching data:"));
        assertTrue(result.contains("API call failed"));
    }

    /**
     * Test to verify that getSpaceXData constructs the correct API URL
     * based on the endpoint parameter provided.
     */
    @Test
    public void testGetSpaceXData_CorrectUrlConstruction() {
        // Setup
        String mockEndpoint = "launches";
        String mockResponse = "[{\"id\": \"1\"}]";
        String mockUrl = baseUrl + "/" + mockEndpoint;

        Mockito.when(restTemplate.getForObject(mockUrl, String.class)).thenReturn(mockResponse);

        // Execute
        String result = spaceXRocketsApi.getSpaceXData(mockEndpoint);

        // Verify
        Mockito.verify(restTemplate).getForObject(mockUrl, String.class);
        assertEquals(mockResponse, result);
    }
}