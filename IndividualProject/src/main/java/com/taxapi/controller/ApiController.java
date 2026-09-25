package com.taxapi.controller;


//Importing specific annoations from instead importing all of using *
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
import com.taxapi.service.TaxApiService;
import org.springframework.web.bind.annotation.PatchMapping;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for the Tax API.
 */
@RestController
@RequestMapping("/v1")
public final class ApiController {

    /** The tax API service. */
    private final TaxApiService taxApiService;

    /**
     * Constructor for the ApiController class.
     * @param taxApiService the service used to interact with tax API
     */
    public ApiController(
        final TaxApiService taxApiService
    ) {
        this.taxApiService = taxApiService;
    }

    /**
     * Creates a new client.
     *
     * @param client the client to create
     * @return the created client
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/clients")
    public ResponseEntity<?> createClient(
        @RequestBody final Client client
    ) throws IOException {
        Client createdClient =
            taxApiService.createClient(
                client.getName()
            );
        if (createdClient == null) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(java.util.Map.of(
                    "error",
                    "A client with that name "
                        + "already exists"
                ));
        }
        return ResponseEntity.ok(createdClient);
    }

    /** 
     * Creates a new item.
     *@param apiKey the API key
     * @param item is the item to create
     * @return the created the item
     * @throws IOException if API key is invalid
     */
    @PostMapping("/items")
    public ResponseEntity<Item> createItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody final Item item
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item createdItem = taxApiService.createItem(
            item.getName(),
            item.getCategory(),
            item.getBasePrice()
        );
        return ResponseEntity.ok(createdItem);  //Missing semicolon error
    }

    /**
     * Function to get all items.
     * @param apiKey is the API key
     * @param category is the category of the items to get
     * @param q is the query to search for
     * @return the list of all items
     * @throws IOException if API key is invalid
     */
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getItems(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestParam(required = false) final String category,
        @RequestParam(required = false) final String q
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        List<Item> items = taxApiService.getItems(category, q);
        return ResponseEntity.ok(items);
    }

    /**
     * Gets an item by its ID.
     *
     * @param apiKey the API key
     * @param id the item ID
     * @return the item if found
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItemById(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item item = taxApiService.getItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }


    /**
     * Function to delete an item.
     * @param apiKey is the API key
     * @param id is the ID of the item to delete
     * @return the deleted item
     * @throws IOException if API key is invalid
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        boolean deleted =
            taxApiService.deleteItem(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

   /**
   * Updates the base price of an existing item.
   * @param apiKey is the API key
   * @param id is the ID of the item to update
   * @param item is the item to update
   * @return the updated item
   * @throws IOException if API key is invalid
   */
  @PatchMapping("/items/{id}")
  public ResponseEntity<Item> updateItemPrice(
    @RequestHeader("X-API-Key")
    final String apiKey,
    @PathVariable final String id,
    @RequestBody final Item item
  ) throws IOException {
    if (!taxApiService.validateApiKey(apiKey)) {
      return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .build();
    }
    Item updatedItem = taxApiService.updateItemPrice(id, item.getBasePrice());
    if (updatedItem == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(updatedItem);
  }
  

    /**
     * Calculates tax for a quote request.
     *
     * @param apiKey the API key
     * @param request the tax quote request
     * @return the tax quote response
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/tax/quote")
    public ResponseEntity<TaxQuoteResponse>
        calculateTax(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody
        final TaxQuoteRequest request
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        TaxQuoteResponse response =
            taxApiService.calculateTax(request);
        if (response == null) {
            return ResponseEntity
                .badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Function to get the supported countries.
     * @param apiKey is the API key
     * @return the supported countries
     * @throws IOException if the API key is invalid or if there is an I/O error
     */
    @GetMapping("/supported")
    public ResponseEntity<SupportedResponse>
        getSupported(
        @RequestHeader("X-API-Key")
        final String apiKey
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        SupportedResponse response =
            taxApiService.getSupported();
        return ResponseEntity.ok(response);
    }
}
