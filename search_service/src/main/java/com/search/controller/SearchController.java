package com.search.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.search.domain.dto.ItemDTO;
import com.search.domain.po.ItemDoc;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@RequiredArgsConstructor
public class SearchController {
    private  final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("192.168.121.162:9200")
    ));


    @GetMapping("/{id}")
    public ItemDTO search(@PathVariable Long id) throws Exception {

        GetRequest request = new GetRequest("item").id(id.toString());

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        if (response.isExists()) {
            String item = response.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(item, ItemDoc.class);
             return BeanUtil.copyProperties(itemDoc, ItemDTO.class);
        } else {
            throw new RuntimeException("Item not found");
        }


    }
}
