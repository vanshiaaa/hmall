package com.api.client;

import com.api.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("search-service")
public interface SearchClient {
    @GetMapping()
    ItemDTO queryItemById(@PathVariable("id") Long id);

}
