package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "bcbClient", url = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata", fallback = CurrencyBCFallback.class)
public interface CurrencyBCClient {

    @GetMapping("/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda='{moeda}'&@dataCotacao='{data}'&$format=json")
    Map<String, Object> getCurrencyString(@RequestParam("moeda") String moeda, @RequestParam("data") String data);
}
