package com.api.transaction.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionSaveEndpointTest {

    @Inject
    private WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenTransactionNotOlderThan1minShouldReturn200() throws Exception {
        double amount = 10.0;
        long timestamp = Instant.now().toEpochMilli();

        String json = "{\"amount\": \"" + amount + "\", \"timestamp\": \"" + timestamp + "\"}";
        mockMvc.perform(
                post("/transactions")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    public void givenTransactionOlderThan1minShouldReturn204() throws Exception {
        double amount = 10.0;
        long timestamp = Instant.now().toEpochMilli() - 100 * 1000;// 100 sec

        String json = "{\"amount\": \"" + amount + "\", \"timestamp\": \"" + timestamp + "\"}";
        mockMvc.perform(
                post("/transactions")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    public void givenTransactionWithTimestampNull_whenSaveTransaction_shouldReturn400() throws Exception {
        double amount = 10.0;
        long timestamp = Instant.now().toEpochMilli();

        String json = "{\"amount\": \"" + amount + "\", \"timestamp\": null}";
        mockMvc.perform(
                post("/transactions")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenTransactionWithAmountNull_whenSaveTransaction_shouldReturn400() throws Exception {
        double amount = 10.0;
        long timestamp = Instant.now().toEpochMilli();

        String json = "{\"amount\": null, \"timestamp\": \"" + timestamp + "\"}";
        mockMvc.perform(
                post("/transactions")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }
}