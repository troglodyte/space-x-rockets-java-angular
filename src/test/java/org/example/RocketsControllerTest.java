package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class RocketsControllerTest {

    private SpaceXRocketsApi api;
    private RocketsController controller;

    @BeforeEach
    void setUp() {
        api = Mockito.mock(SpaceXRocketsApi.class);
        controller = new RocketsController(api);
    }

    @Test
    void all_returnsAllRocketsMapped() throws Exception {
        String json = "[" +
                "{\"id\":\"r1\",\"name\":\"Falcon 1\",\"active\":false,\"success_rate_pct\":40}," +
                "{\"id\":\"r2\",\"name\":\"Falcon 9\",\"active\":true,\"success_rate_pct\":97}," +
                "{\"id\":\"r3\",\"name\":\"Starship\",\"active\":true,\"success_rate_pct\":10}" +
                "]";
        when(api.getRocketsData()).thenReturn(json);

        List<?> result = controller.all(null);
        assertEquals(3, result.size(), "Should map all rockets to DTOs");
    }

    @Test
    void active_filtersOnlyActiveRockets() throws Exception {
        String json = "[" +
                "{\"id\":\"r1\",\"name\":\"Falcon 1\",\"active\":false,\"success_rate_pct\":40}," +
                "{\"id\":\"r2\",\"name\":\"Falcon 9\",\"active\":true,\"success_rate_pct\":97}," +
                "{\"id\":\"r3\",\"name\":\"Starship\",\"active\":true,\"success_rate_pct\":10}" +
                "]";
        when(api.getRocketsData()).thenReturn(json);

        List<?> result = controller.active();
        assertEquals(2, result.size(), "Should include only active rockets");
    }
}
