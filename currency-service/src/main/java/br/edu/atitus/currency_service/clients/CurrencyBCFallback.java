package br.edu.atitus.currency_service.clients;

import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CurrencyBCFallback implements CurrencyBCClient {
    @Override
    public Map<String, Object> getCurrencyString(String moeda, String data) {
        return Collections.emptyMap();
    }
}
