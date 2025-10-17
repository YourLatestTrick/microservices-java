package br.edu.atitus.currency_service.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.clients.CurrencyBCClient;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyBCClient bcbClient;

    private static final String SERVICE_NAME = "CurrencyBCClientgetCurrencyString";

    @GetMapping("/currency/{source}/{target}")
    @Cacheable(value = "currencyCache", key = "#source + '-' + #target")
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "fallbackCurrency")
    public CurrencyEntity getCurrency(@PathVariable String source, @PathVariable String target) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        Map<String, Object> response = bcbClient.getCurrencyString(source, today);

        if (response != null && response.containsKey("value")) {
            Object[] values = ((java.util.List<Object>) response.get("value")).toArray();
            if (values.length > 0) {
                Map<String, Object> data = (Map<String, Object>) values[0];
                double rate = Double.parseDouble(data.get("cotacaoVenda").toString());
                CurrencyEntity entity = new CurrencyEntity();
                entity.setSource(source);
                entity.setTarget(target);
                entity.setConversionFactor(rate);
                entity.setEnvironment("API BCB");
                return entity;
            }
        }
        throw new RuntimeException("Sem dados de cotação na API BCB");
    }

    public CurrencyEntity fallbackCurrency(String source, String target, Throwable t) {
        Optional<CurrencyEntity> optional = currencyRepository.findBySourceAndTarget(source, target);
        if (optional.isPresent()) {
            CurrencyEntity entity = optional.get();
            entity.setEnvironment("Local Database");
            return entity;
        }

        CurrencyEntity empty = new CurrencyEntity();
        empty.setSource(source);
        empty.setTarget(target);
        empty.setConversionFactor(-1.0);
        empty.setEnvironment("Fallback sem dados");
        return empty;
    }
}
