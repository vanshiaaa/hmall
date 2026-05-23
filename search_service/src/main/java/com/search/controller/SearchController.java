package com.search.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.search.domain.dto.ItemDTO;
import com.search.domain.po.ItemDoc;
import com.search.domain.query.ItemPageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
public class SearchController {
    private  final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("192.168.121.162:9200")
    ));


    @GetMapping("/{id}")
    public ItemDTO search(@PathVariable Long id) throws Exception {

        GetRequest request = new GetRequest("items").id(id.toString());

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        if (response.isExists()) {
            String item = response.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(item, ItemDoc.class);
             return BeanUtil.copyProperties(itemDoc, ItemDTO.class);
        } else {
            throw new RuntimeException("Item not found");
        }


    }
    @GetMapping("/list")
    @ApiOperation("搜索接口")
    public PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException {
        // 1.构建查询条件
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // 2.执行查询
        if (StrUtil.isNotBlank(query.getKey())){
            bool.must().add(QueryBuilders.matchQuery("name",query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())){
            bool.filter().add(QueryBuilders.termQuery("brand",query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())){
            bool.filter().add(QueryBuilders.termQuery("category",query.getCategory()));
        }
        if (query.getMinPrice() != null){
            bool.filter().add(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        if (query.getMaxPrice() != null){
            bool.filter().add(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }
        request.source().query(bool);
        // 2.1 设置分页参数
        request.source().from((query.getPageNo() - 1) * query.getPageSize()).size(query.getPageSize());
        // 2.2 设置排序
        if (StrUtil.isNotBlank(query.getSortBy())){
            request.source().sort(query.getSortBy(),query.getIsAsc() ? SortOrder.DESC : SortOrder.ASC);
        }
        // 2.3 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 2.4 解析
        PageDTO<ItemDoc> result = parseresult(response);
        // 3.封装并返回
        return result;

    }


    private PageDTO<ItemDoc> parseresult(SearchResponse response){
        List<ItemDoc> list = new ArrayList<>();
        // 总条数
        Long total = response.getHits().getTotalHits().value;

        SearchHits searchHits = response.getHits();

        SearchHit[] hits = searchHits.getHits();

        for (SearchHit hit : hits) {
            list.add(JSONUtil.toBean(hit.getSourceAsString(),ItemDoc.class));
        }
        return new PageDTO<>(total,10L,list);
    }

    @ApiOperation("过滤聚合方法")
    @PostMapping("/filters")
    public Map <String, List<String>> filters(@RequestBody ItemPageQuery query) throws IOException {
        // TODO 1.构建查询条件，执行查询
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // 2.执行查询
        if (StrUtil.isNotBlank(query.getKey())){
            bool.must().add(QueryBuilders.matchQuery("name",query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())){
            bool.filter().add(QueryBuilders.termQuery("brand",query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())){
            bool.filter().add(QueryBuilders.termQuery("category",query.getCategory()));
        }
        if (query.getMinPrice() != null){
            bool.filter().add(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        if (query.getMaxPrice() != null){
            bool.filter().add(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }
        request.source().query(bool);

        // TODO 2.解析聚合结果并返回
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(20));
        request.source().aggregation(AggregationBuilders.terms("categoryAgg").field("category").size(20));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Aggregation brandAgg = aggregations.get("brandAgg");
        Aggregation categoryAgg = aggregations.get("categoryAgg");
        Map<String, List<String>> result =  new HashMap<>();
            result.put("brand",((org.elasticsearch.search.aggregations.bucket.terms.Terms) brandAgg).getBuckets().stream().map(bucket -> bucket.getKeyAsString()).toList());
            result.put("category",((org.elasticsearch.search.aggregations.bucket.terms.Terms) categoryAgg).getBuckets().stream().map(bucket -> bucket.getKeyAsString()).toList());
            return result;
    }
}
