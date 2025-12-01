package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class RestTemplateConfigTest {

    @Test
    void restTemplateBean_isCreated() {
        RestTemplate bean = new RestTemplateConfig().restTemplate();
        assertNotNull(bean);
    }
}
