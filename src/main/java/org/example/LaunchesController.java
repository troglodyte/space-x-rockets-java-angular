package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * REST Controller for handling SpaceX launches related endpoints.
 * Provides functionality to retrieve and filter launch information.
 *
 * @author Michael Harris
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/launches")
public class LaunchesController {
    /**
     * Internal class representing a SpaceX launch.
     * Includes basic launch information such as ID, name, rocket, and date.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Launch {
        private String id;
        private String name;
        private String rocket;
        /**
         * The Unix timestamp of the launch date.
         * Stored as Integer to match SpaceX API format.
         */
        private Integer date_unix;

        /**
         * Default constructor for Launch class.
         * Required for JSON deserialization.
         */
        public Launch() {
        }

        /**
         * Gets the unique identifier of the launch.
         *
         * @return the launch ID
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the unique identifier of the launch.
         *
         * @param id the launch ID to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Gets the name of the launch.
         *
         * @return the launch name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of the launch.
         *
         * @param name the launch name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the rocket identifier used for the launch.
         *
         * @return the rocket ID
         */
        public String getRocket() {
            return rocket;
        }

        /**
         * Sets the rocket identifier for the launch.
         *
         * @param rocket the rocket ID to set
         */
        public void setRocket(String rocket) {
            this.rocket = rocket;
        }

        public String getDate() {
            // Some launches may not have a date_unix field; return empty string to keep mapping safe
            if (this.date_unix == null) {
                return "";
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US);
            return Instant.ofEpochSecond(this.date_unix)
                    .atZone(ZoneId.of("America/New_York"))
                    .format(formatter);
        }

        public void setDate_unix(Integer dateUnix) {
            this.date_unix = dateUnix;
        }
    }

    /**
     * Data Transfer Object for launch information.
     * Contains simplified launch data for API responses.
     *
     * @param id     The unique identifier of the launch (cannot be null)
     * @param name   The name of the launch (cannot be null)
     * @param rocket The rocket ID used for the launch (cannot be null)
     * @param date   The formatted date of the launch in "MMM dd, yyyy" format
     * @since 1.0
     */
    public record LaunchDTO(String id, String name, String rocket, String date) {
    }

    /**
     * Service for accessing SpaceX API endpoints.
     * Handles all HTTP communications with the SpaceX API.
     */
    private final SpaceXRocketsApi spaceXRocketsApi;

    /**
     * JSON object mapper for data serialization/deserialization.
     * Thread-safe instance used for converting JSON data to Java objects.
     */
    private final ObjectMapper objectMapper;

    @Autowired
    public LaunchesController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves all launches for a specific rocket ID.
     * Filters the complete launch data set to return only launches
     * associated with the specified rocket.
     *
     * @param rocketId The ID of the rocket to filter launches by (cannot be null)
     * @return List of launches associated with the specified rocket, may be empty but never null
     * @throws Exception if there's an error retrieving or processing the launch data
     * @since 1.0
     */
    @GetMapping(value = "/id/{rocketId}", produces = "application/json")
    @ResponseBody
    protected List<LaunchDTO> id(@PathVariable String rocketId) throws Exception {
        String data = this.spaceXRocketsApi.getAllLaunchesData();
        List<Launch> launches = objectMapper.readValue(data, new TypeReference<List<Launch>>() {
        });
        List<LaunchDTO> filteredLaunches = launches.stream()
                // Use null-safe comparison in case some entries miss the rocket field
                .filter(launch -> rocketId.equals(launch.getRocket()))
                .map(launch -> new LaunchDTO(
                        launch.getId(),
                        launch.getName(),
                        launch.getRocket(),
                        launch.getDate()
                ))
                .toList();
        return filteredLaunches.isEmpty()
                ? List.of(new LaunchDTO("", "No Launches", "", ""))
                : filteredLaunches;
    }
}
