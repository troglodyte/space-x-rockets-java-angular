package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/launches")
public class LaunchesController {
    private final SpaceXRocketsApi spaceXRocketsApi;

    @Autowired
    public LaunchesController(SpaceXRocketsApi spaceXRocketsApi) {
        this.spaceXRocketsApi = spaceXRocketsApi;
    }

    @GetMapping(value = "/all", produces = "application/json")
    @ResponseBody
    public String all() throws Exception {
        return this.spaceXRocketsApi.getLaunchesData();
    }

}
