package com.aeon.product;

import com.aeon.product.dto.InventoryDtos.ReserveItem;
import com.aeon.product.dto.InventoryDtos.ReserveRequest;
import com.aeon.product.repository.ProductRepository;
import com.aeon.product.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiIT {

    private static final String NEW_PRODUCT = """
            {"name":"Orbit One","collection":"Orbit","description":"Test watch","price":499.00,"stock":4,
             "caseMaterial":"Steel","movement":"Quartz","diameterMm":38,"waterResistanceM":50,
             "caseColor":"#AAAAAA","dialColor":"#000000","strapColor":"#111111","accentColor":"#00FFFF",
             "customizable":true}""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private InventoryService inventory;
    @Autowired
    private ProductRepository products;

    @Test
    void listsSeededCatalogWithPagination() throws Exception {
        mvc.perform(get("/api/products").param("size", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(10)));
    }

    @Test
    void searchFiltersByTextAndPrice() throws Exception {
        mvc.perform(get("/api/products").param("q", "abyss").param("maxPrice", "120000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Abyss Diver 300"));
    }

    @Test
    void searchMatchesMovement() throws Exception {
        mvc.perform(get("/api/products").param("q", "moonphase"))
                .andExpect(jsonPath("$.content[0].name").value("Solaris Eclipse"));
    }

    @Test
    void sortsByPriceDescending() throws Exception {
        mvc.perform(get("/api/products").param("sort", "price,desc").param("size", "1"))
                .andExpect(jsonPath("$.content[0].name").value("Nebula Aurora"));
    }

    @Test
    void detailsAndNotFound() throws Exception {
        mvc.perform(get("/api/products/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 99999"));
    }

    @Test
    void collectionsAndCustomizations() throws Exception {
        mvc.perform(get("/api/products/collections"))
                .andExpect(jsonPath("$", hasItem("Nebula")));
        mvc.perform(get("/api/customizations"))
                .andExpect(jsonPath("$.parts.CASE.length()").value(4))
                .andExpect(jsonPath("$.parts.HANDS[0].id").value("hands-ice"));
    }

    @Test
    void adminCrudRequiresAdminRole() throws Exception {
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/products").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT))
                .andExpect(status().isForbidden());

        String created = mvc.perform(post("/api/admin/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(put("/api/admin/products/" + id).header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT.replace("Orbit One", "Orbit Two")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Orbit Two"));

        mvc.perform(delete("/api/admin/products/" + id).header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void adminValidationErrors() throws Exception {
        mvc.perform(post("/api/admin/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_PRODUCT.replace("\"#AAAAAA\"", "\"red\"").replace("499.00", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.caseColor").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    void reserveIsAllOrNothing() throws Exception {
        int stockBefore = products.findById(3L).orElseThrow().getStock();
        String body = """
                {"items":[{"productId":3,"quantity":1},{"productId":10,"quantity":10}]}""";

        mvc.perform(post("/internal/inventory/reserve").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        assertThat(products.findById(3L).orElseThrow().getStock()).isEqualTo(stockBefore);
    }

    @Test
    void concurrentReservationsNeverOversell() throws Exception {
        String created = mvc.perform(post("/api/admin/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_PRODUCT.replace("\"stock\":4", "\"stock\":3")))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> attempts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            attempts.add(() -> {
                try {
                    inventory.reserve(new ReserveRequest(List.of(new ReserveItem(id, 1, null))));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
        }
        int successes = 0;
        for (Future<Boolean> f : pool.invokeAll(attempts)) {
            if (f.get()) {
                successes++;
            }
        }
        pool.shutdown();

        assertThat(successes).isEqualTo(3);
        assertThat(products.findById(id).orElseThrow().getStock()).isZero();
    }

    @Test
    void quotePricesCustomizations() throws Exception {
        mvc.perform(post("/internal/inventory/quote").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1,\"optionIds\":[\"case-ceramic\",\"hands-solar\"]}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(150400.00))
                .andExpect(jsonPath("$.lines[*].quantity", everyItem(greaterThanOrEqualTo(1))));
    }
}
