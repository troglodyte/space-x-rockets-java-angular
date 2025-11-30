package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

@RestController
@RequestMapping("/api/launches")
public class LaunchesController {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Launch {
        private String id;
        private String name;
        private String rocket;

        public Launch() {
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

        public String getRocket() {
            return rocket;
        }

        public void setRocket(String rocket) {
            this.rocket = rocket;
        }
    }

    private static class LaunchDTO {
        private final String id;
        private final String name;
        private final String rocket;

        public LaunchDTO(String id, String name, String rocket) {
            this.id = id;
            this.name = name;
            this.rocket = rocket;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRocket() {
            return rocket;
        }
    }

    private final SpaceXRocketsApi spaceXRocketsApi;
    private final ObjectMapper objectMapper;

    @Autowired
    public LaunchesController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping(value = "/id/{rocketId}", produces = "application/json")
    @ResponseBody
    public List<LaunchDTO> id(@PathVariable String rocketId) throws Exception {
        String data = this.spaceXRocketsApi.getAllLaunchesData();
        List<Launch> launches = objectMapper.readValue(data, new TypeReference<List<Launch>>() {
        });
        return launches.stream()
                .filter(launch -> launch.getRocket().equals(rocketId))
                .map(launch -> new LaunchDTO(
                        launch.getId(),
                        launch.getName(),
                        launch.getRocket()
                ))
                .toList();
    }
}
