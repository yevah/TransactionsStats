package com.api.statistics.controller;

import com.api.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.time.Instant;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class StatisticsEndpointTest {
    @Inject
    private WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    //end to end test. First we will call /transactions to save transaction and then /statistics to get statistics.
    @Test
    public void givenTransactionSaved_whenCallStatistics_thenStatisticsJsonShouldBeReturned() throws Exception {
        double amount = 10.0;
        long timestamp = Instant.now().toEpochMilli();
        String json = "{\"amount\": \"" + amount + "\", \"timestamp\": \"" + timestamp + "\"}";
        mockMvc.perform(
                post("/transactions")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(
                get("/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("max", is(amount)))
                .andExpect(jsonPath("min", is(amount)))
                .andExpect(jsonPath("count", is(1)))
                .andExpect(jsonPath("avg", is(amount)));


    }

}