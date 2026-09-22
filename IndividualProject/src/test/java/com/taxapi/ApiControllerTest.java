package com.taxapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LocalStorageService localStorageService;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    protected static final String VALID_KEY = "valid-key";

    // sometimes i just use the string directly lol
    private static final String BAD_KEY = "nope";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    @Test
    void getItems_worksWithGoodKey() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getItems_badKey_shouldBe401() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", BAD_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getOneItem_ok() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("item-1"));
    }

    @Test
    void deleteItem_happyPath() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNoContent());
    }

    @Test
    void postItem_ok() throws Exception {
        String body = "{\"name\":\"Mug\",\"category\":\"home\",\"basePrice\":12.5}";
        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mug"));
    }

    // createClient only succeeds when name already exists (Alice) - checked the service code
    @Test
    void createClient_alice_ok() throws Exception {
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Alice\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void taxQuote_byItem_ok() throws Exception {
        String json = "{\"state\":\"CA\",\"itemId\":\"item-1\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void supported_ok() throws Exception {
        mockMvc.perform(get("/v1/supported")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.states").isArray());
    }
}
