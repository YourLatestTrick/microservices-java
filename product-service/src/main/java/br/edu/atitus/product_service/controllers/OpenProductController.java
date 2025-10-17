package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.entities.ProductEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class OpenProductController {

    @Autowired
    private CurrencyClient currencyClient;

    private static final String PRODUCT_CB = "productServiceCB";

    @GetMapping("/product/{id}/{currency}")
    @Cacheable(value = "productCache", key = "#id + '-' + #currency")
    @CircuitBreaker(name = PRODUCT_CB, fallbackMethod = "fallbackProduct")
    public ProductEntity getProduct(@PathVariable Long id, @PathVariable String currency) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName("Produto " + id);
        product.setPrice(100.0);

        try {
            double rate = currencyClient.getCurrency(id.toString(), currency).getConversionRate();
            product.setConvertedPrice(product.getPrice() * rate);
        } catch (Exception e) {
            product.setConvertedPrice(-1);
        }
        product.setEnvironment("Product Service OK");
        return product;
    }

    public ProductEntity fallbackProduct(Long id, String currency, Throwable t) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName("Produto " + id);
        product.setPrice(100.0);
        product.setConvertedPrice(-1);
        product.setEnvironment("Fallback - Currency Service offline");
        return product;
    }
}
