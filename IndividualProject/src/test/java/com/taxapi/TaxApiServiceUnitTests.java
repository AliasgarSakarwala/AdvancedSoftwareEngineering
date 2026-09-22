package com.taxapi;

import com.taxapi.service.TaxApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;

import com.taxapi.model.Item;
import com.taxapi.model.Client;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import(TestConfig.class)
class TaxApiServiceUnitTests {

    @Autowired
    private TaxApiService service;

    @Autowired
    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // TODO(student): add @Test methods that exercise TaxApiService directly.
    // The `service` field above is the autowired bean under test.

    //Checks if API Key is valid
    @Test
    void validateAPIKey_withValidKey() throws Exception {
    assertTrue(service.validateApiKey("valid-key"));
    }

    @Test
    void validateAPIKey_withInvalidKey() throws Exception {
        assertFalse(service.validateApiKey("invalid-key"));
    }

    @Test
    void getItem_by_ID_withValidID() throws Exception {
        Item item = service.getItemById("item-1");
        assertNotNull(item);
        assertEquals("item-1", item.getId());
        assertEquals("Laptop", item.getName());
    }

    @Test
    void getItem_by_ID_withInvalidID() throws Exception {
        Item item = service.getItemById("invalid-id");
        assertNull(item);
    }

    @Test
    void deleteItem_whenItemExists() throws Exception {
        assertTrue(service.deleteItem("item-1"));
        assertNull(service.getItemById("item-1"));
    }

    @Test
    void createItem_addsNewOne() throws Exception {
        Item created = service.createItem("Socks", "clothing", 10.00);
        assertNotNull(created);
        assertEquals("Socks", created.getName());
    }

    @Test
    void getItems_shouldHaveOneAtStart() throws Exception {
        assertEquals(1, service.getItems().size());
    }

    @Test
    void createClient_aliceAlreadyThere() throws Exception {
        // duplicate name should be rejected
        assertNull(service.createClient("Alice"));
    }

    @Test
    void createClient_brandNewName_ok() throws Exception {
        Client c = service.createClient("Bob");
        assertNotNull(c);
        assertEquals("Bob", c.getName());
        assertNotNull(c.getApiKey());
    }

    @Test
    void deletItem_whenItemDoesNotExist() throws Exception {
        assertFalse(service.deleteItem("NotItem1"));
    }

    @Test
    void calculateTax_withItemID() throws Exception {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setItemId("item-1");
        request.setState("CA");

        TaxQuoteResponse response = service.calculateTax(request);

        assertNotNull(response);
        assertEquals(999.99, response.getPrice(), 0.001);
        assertEquals(72.499275, response.getTaxAmount(), 0.001);
        assertEquals(1072.489275, response.getTotal(), 0.001);
    }

    @Test
    void getSupported_stuff() throws Exception {
        SupportedResponse res = service.getSupported();
        assertTrue(res.getStates().contains("CA"));
        assertTrue(res.getCategories().contains("electronics"));
    }
}
