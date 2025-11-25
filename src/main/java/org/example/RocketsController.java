package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/rockets")
public class RocketsController {
    private final SpaceXRocketsApi spaceXRocketsApi;

    @Autowired
    public RocketsController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
    }

    @GetMapping(value = "/all", produces = "application/json")
    @ResponseBody
    public String all() throws Exception {
        return this.spaceXRocketsApi.getRocketsData();
    }
}
