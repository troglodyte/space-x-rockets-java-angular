package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/api/rockets")
public class RocketsController {
    private final ObjectMapper objectMapper;

    // id, name, active, success_rate_pct
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Rocket {
        private String id;
        private String name;
        private Boolean active;
        private Integer success_rate_pct;

        public Rocket() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public Integer getSuccess_rate_pct() { return success_rate_pct; }
        public void setSuccess_rate_pct(Integer success_rate_pct) { this.success_rate_pct = success_rate_pct; }
    }

    private static class RocketDTO {
        private final String id;
        private final String name;
        private final Boolean active;
        private final Integer successRatePct;

        public RocketDTO(String id, String name, Boolean active, Integer successRatePct) {
            this.id = id;
            this.name = name;
            this.active = active;
            this.successRatePct = successRatePct;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Boolean getActive() { return active; }
        public Integer getSuccessRatePct() { return successRatePct; }
    }

    private final SpaceXRocketsApi spaceXRocketsApi;

    @Autowired
    public RocketsController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
        this.objectMapper = new ObjectMapper();
    }


    @GetMapping(value = "/all", produces = "application/json")
    @ResponseBody
    public List<RocketDTO> all() throws Exception {
        List<Rocket> rockets = getParsedResponse();
        return getRocketsDTO(rockets);
    }

    private List<Rocket> parseRockets(String response) throws JsonProcessingException {
        return this.objectMapper.readValue(response, new TypeReference<List<Rocket>>() {});
    }

    /**
     * Intent: Parse the api response and only show active rockets.
     * @return List of RocketDTO objects
     * @throws Exception Could not parse JSON response
     */
    @GetMapping(value = "/active", produces = "application/json")
    @ResponseBody
    public List<RocketDTO> active() throws Exception {
        List<Rocket> rockets = getParsedResponse();
        List<Rocket> activeRockets = getActiveRockets(rockets);
        return getActiveRocketsDTO(activeRockets);
    }

    /**
     * Get a List of rockets from the SpaceX API and parse it into a List of Rocket objects.
     * @return List
     * @throws JsonProcessingException Could not process JSON response
     */
    private List<Rocket> getParsedResponse() throws JsonProcessingException {
        String response = this.spaceXRocketsApi.getRocketsData();
        return parseRockets(response);
    }

    /**
     * Intent: Filter out inactive rockets.
     * @param rockets List of rockets
     * @return rockets Filtered list of only active rockets
     */
    private static List<Rocket> getActiveRockets(List<Rocket> rockets) {
        return rockets.stream()
                .filter(rocket -> Boolean.TRUE.equals(rocket.getActive()))
                .toList();
    }

    /**
     * Intent: Filter out response data to only include the data we want to see.
     * @param rockets List of rockets from SpaceX API
     * @return rockets Filtered list of rockets with only the data we want to see
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
     * Intent: Filter out response data to only include the data we want to see.
     * @param rockets List of rockets from SpaceX API
     * @return rockets Filtered list of rockets with only the data we want to see
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
