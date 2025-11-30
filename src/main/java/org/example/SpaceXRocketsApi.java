package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import org.springframework.stereotype.Service;


@Service
public class SpaceXRocketsApi {
    private final RestTemplate restTemplate;

    @Value("${spacex.api.url}")
    private String spaceXApiUrl;

    @Autowired
    public SpaceXRocketsApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch data from a given API endpoint
     *
     * @param url The API endpoint URL
     * @return The response as a String
     */
    protected String getApiResponse(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (org.springframework.web.client.RestClientException e) {
            return "Error fetching data: " + e.getMessage();
        }
    }

    /**
     * Fetch JSON data and convert to a specific class
     *
     * @param url          The API endpoint URL
     * @param responseType The class to convert the response to
     * @return The parsed response object
     */
    public <T> T getApiResponse(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (RestClientException e) {
            System.err.println("Error fetching data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetch data from SpaceX API
     *
     * @param endpoint The SpaceX API endpoint (e.g., "rockets", "launches")
     * @return The API response as a String
     */
    protected String getSpaceXData(String endpoint) {
        String url = spaceXApiUrl + "/" + endpoint;
        return getApiResponse(url);
    }

    public String getRocketsData() {
        return getSpaceXData("v4/rockets");
    }

    public String getAllLaunchesData() {
        return getSpaceXData("v4/launches");
    }
}
