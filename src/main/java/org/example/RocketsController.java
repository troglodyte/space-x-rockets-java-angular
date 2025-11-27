package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    // Rocket record modeled after SpaceX v4 /rockets schema
    // The annotations instruct Jackson to ignore any properties we don't explicitly model here.
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Rocket(
            String id,
            String name,
            String type,
            Boolean active,
            Integer stages,
            Integer boosters,
            Integer cost_per_launch,
            Integer success_rate_pct,
            String first_flight,
            String country,
            String company,
            Dimension height,
            Dimension diameter,
            Mass mass,
            FirstStage first_stage,
            SecondStage second_stage,
            Engines engines,
            LandingLegs landing_legs,
            List<String> flickr_images,
            String wikipedia,
            String description
    ) {}

    private record RocketDTO(
            String id,
            String name,
            String description,
            Boolean active,
            Integer successRatePct
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Dimension(Double meters, Double feet) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Mass(Integer kg, Integer lb) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thrust(Double kN, Double lbf) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Isp(Integer sea_level, Integer vacuum) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FirstStage(
            Boolean reusable,
            Integer engines,
            Double fuel_amount_tons,
            Integer burn_time_sec,
            Thrust thrust_sea_level,
            Thrust thrust_vacuum
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CompositeFairing(Dimension height, Dimension diameter) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Payloads(CompositeFairing composite_fairing, String option_1) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SecondStage(
            Boolean reusable,
            Integer engines,
            Double fuel_amount_tons,
            Integer burn_time_sec,
            Thrust thrust,
            Payloads payloads
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Engines(
            Integer number,
            String type,
            String version,
            String layout,
            Isp isp,
            Integer engine_loss_max,
            String propellant_1,
            String propellant_2,
            Thrust thrust_sea_level,
            Thrust thrust_vacuum,
            Double thrust_to_weight
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LandingLegs(Integer number, String material) {}

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
        return getActiveRocketsDTO(rockets);
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
                .filter(rocket -> rocket.active() != null && rocket.active())
                .toList();
    }

    /**
     * Intent: Filter out response data to only include the data we want to see.
     * @param rockets List of rockets from SpaceX API
     * @return rockets Filtered list of rockets with only the data we want to see
     */
    private static List<RocketDTO> getActiveRocketsDTO(List<Rocket> rockets) {
        return rockets.stream()
                .filter(rocket -> Boolean.TRUE.equals(rocket.active()))
                .map(rocket -> new RocketDTO(
                        rocket.id(),
                        rocket.name(),
                        rocket.country(),
                        rocket.active(),
                        rocket.success_rate_pct()
                ))
                .toList();
    }
}
