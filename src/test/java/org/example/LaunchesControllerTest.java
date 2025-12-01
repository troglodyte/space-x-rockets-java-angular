package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class LaunchesControllerTest {

    private SpaceXRocketsApi api;
    private LaunchesController controller;

    @BeforeEach
    void setUp() {
        api = Mockito.mock(SpaceXRocketsApi.class);
        controller = new LaunchesController(api);
    }

    @Test
    void id_filtersLaunchesByRocketId() throws Exception {
        String json = "[" +
                "{\"id\":\"l1\",\"name\":\"CRS-1\",\"rocket\":\"r2\"}," +
                "{\"id\":\"l2\",\"name\":\"Starlink 1\",\"rocket\":\"r2\"}," +
                "{\"id\":\"l3\",\"name\":\"Demo\",\"rocket\":\"r9\"}" +
                "]";
        when(api.getAllLaunchesData()).thenReturn(json);

        List<?> filtered = controller.id("r2");
        assertEquals(2, filtered.size(), "Should return launches for the specified rocket id only");
    }
}
