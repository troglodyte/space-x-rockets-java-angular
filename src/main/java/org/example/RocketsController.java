package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Comparator;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * REST Controller for handling SpaceX rockets related endpoints.
 * Provides functionality to retrieve and filter rocket information.
 *
 * @author Michael Harris
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/rockets")
public class RocketsController {
    private final ObjectMapper objectMapper;

    // id, name, active, success_rate_pct

    /**
     * Internal class representing a SpaceX rocket.
     * Includes basic rocket information such as ID, name, active status, and success rate.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Rocket {
        private String id;
        private String name;
        private Boolean active;
        private Integer success_rate_pct;

        public Rocket() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Integer getSuccess_rate_pct() {
            return success_rate_pct;
        }

        public void setSuccess_rate_pct(Integer success_rate_pct) {
            this.success_rate_pct = success_rate_pct;
        }
    }

    /**
     * Data Transfer Object for rocket information.
     * Contains simplified rocket data for API responses.
     *
     * @param id             The unique identifier of the rocket
     * @param name           The name of the rocket
     * @param active         Whether the rocket is currently active
     * @param successRatePct The success rate percentage of the rocket
     */
    public record RocketDTO(String id, String name, Boolean active, Integer successRatePct) {
    }

    private final SpaceXRocketsApi spaceXRocketsApi;

    /**
     * Constructs a new RocketsController with the specified SpaceX API client.
     *
     * @param spaceXRocketsApi the API client for SpaceX rockets data
     */
    @Autowired
    public RocketsController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
        this.objectMapper = new ObjectMapper();
    }


//    /**
//     * Retrieves all rockets from the SpaceX API.
//     *
//     * @return List of all rockets as DTOs
//     * @throws Exception if there's an error retrieving or processing the rocket data
//     */
//    @GetMapping(value = "/all", produces = "application/json")
//    @ResponseBody
//    public List<RocketDTO> all() throws Exception {
//        List<Rocket> rockets = getParsedResponse();
//        return getRocketsDTO(rockets);
//    }

    /**
     * Retrieves all rockets sorted by the specified field.
     * todo Note: I've added the sortBy option for an example of how I would approach sorting 
     *   if requested from the front end, however, this is not the approach I would take in production. 
     *   So I left it to the front end to reduce the number of calls to the back end. 
     *
     * todo: to complete this, I would also implement a 'reverse' option for sorting.
     * @param sortBy the field to sort by (id, name, active, or success_rate_pct)
     * @return List of sorted rockets as DTOs
     * @throws Exception if there's an error retrieving or processing the rocket data
     */
    @GetMapping(value = "/all", produces = "application/json")
    @ResponseBody
    public List<RocketDTO> all(@RequestParam(value = "sort", required = false) String sortBy) throws Exception {
        List<Rocket> rockets = getParsedResponse();

        try {
            Comparator<Rocket> comparator = switch (sortBy.toLowerCase()) {
                case "id" -> Comparator.comparing(Rocket::getId);
                case "name" -> Comparator.comparing(Rocket::getName);
                case "active" -> (r1, r2) -> Boolean.compare(r1.getActive(), r2.getActive());
                case "success_rate_pct" -> Comparator.comparing(Rocket::getSuccess_rate_pct,
                        Comparator.nullsLast(Comparator.reverseOrder()));
                default -> null;
            };

            if (comparator != null) {
                rockets = rockets.stream()
                        .sorted(comparator)
                        .toList();
            }
        } catch (Exception e) {
            // Return unsorted list in case of any error
        }

        return getRocketsDTO(rockets);
    }

    /**
     * Parses JSON response into a list of Rocket objects.
     *
     * @param response the JSON string to parse
     * @return List of Rocket objects
     * @throws JsonProcessingException if JSON parsing fails
     */
    private List<Rocket> parseRockets(String response) throws JsonProcessingException {
        return this.objectMapper.readValue(response, new TypeReference<List<Rocket>>() {
        });
    }

    /**
     * Retrieves all active rockets from the SpaceX API.
     *
     * @return List of active rockets as DTOs
     * @throws Exception if there's an error retrieving or processing the rocket data
     */
    @GetMapping(value = "/active", produces = "application/json")
    @ResponseBody
    public List<RocketDTO> active() throws Exception {
        List<Rocket> rockets = getParsedResponse();
        List<Rocket> activeRockets = getActiveRockets(rockets);
        return getActiveRocketsDTO(activeRockets);
    }

    /**
     * Retrieves and parses rocket data from the SpaceX API.
     *
     * @return List of parsed Rocket objects
     * @throws JsonProcessingException if JSON parsing fails
     */
    private List<Rocket> getParsedResponse() throws JsonProcessingException {
        String response = this.spaceXRocketsApi.getRocketsData();
        return parseRockets(response);
    }

    /**
     * Filters the list of rockets to return only active ones.
     *
     * @param rockets the list of rockets to filter
     * @return List of active rockets
     */
    private static List<Rocket> getActiveRockets(List<Rocket> rockets) {
        return rockets.stream()
                .filter(rocket -> Boolean.TRUE.equals(rocket.getActive()))
                .toList();
    }

    /**
     * Converts a list of active rockets to DTOs.
     *
     * @param rockets the list of rockets to convert
     * @return List of active rocket DTOs
     */
    private static List<RocketDTO> getActiveRocketsDTO(List<Rocket> rockets) {
        return rockets.stream()
                .filter(rocket -> Boolean.TRUE.equals(rocket.getActive()))
                .map(rocket -> new RocketDTO(
                        rocket.getId(),
                        rocket.getName(),
                        rocket.getActive(),
                        rocket.getSuccess_rate_pct()
                ))
                .toList();
    }

    /**
     * Converts a list of rockets to DTOs.
     *
     * @param rockets the list of rockets to convert
     * @return List of rocket DTOs
     */
    private static List<RocketDTO> getRocketsDTO(List<Rocket> rockets) {
        return rockets.stream()
                .map(rocket -> new RocketDTO(
                        rocket.getId(),
                        rocket.getName(),
                        rocket.getActive(),
                        rocket.getSuccess_rate_pct()
                ))
                .toList();
    }
}
