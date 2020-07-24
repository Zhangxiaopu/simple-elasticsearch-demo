package com.yaologos.searchhouse.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaologos.searchhouse.entity.User;
import com.yaologos.searchhouse.entity.UserSearch;
import com.yaologos.searchhouse.service.SearchService;
import com.yaologos.searchhouse.service.UserService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean index(String username) {
        // 从数据库读取用户信息，然后填充至UserSearch类中，转为json，提交给elastic
        User user = userService.findUserByName(username);

        if (user == null){
            logger.error("Not Found User类");
            return false;
        }
        //对象类型转换并赋值。属性不一样的，不会赋值。
        UserSearch userSearch = modelMapper.map(user, UserSearch.class);
        try {
            //user是倒叙索引，id是文档类型，source中可以用XContentBuilder 来构建查询参数
            IndexRequest request = new IndexRequest("user").id(String.valueOf(user.getId()))
                    .source(objectMapper.writeValueAsBytes(userSearch), XContentType.JSON);
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            //201表示已经创建成功，可以看下具体的枚举，切勿和大家平时使用的标识代码号，对号入座
            if (response.status().getStatus() != 201){
                logger.error("索引未创建成功");
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    /**
     *
     * @param username
     * @return
     */
    @Override
    public boolean remove(String username) {
        if(StringUtils.isEmpty(username)){
            return false;
        }
        User user = userService.findUserByName(username);
        if(Objects.isNull(user)){
            logger.error("未查找到信息");
            return false;
        }
        //删除请求，删除倒序索引是user和主键编号
        DeleteRequest request = new DeleteRequest("user",String.valueOf(user.getId()));
        try {
            DeleteResponse response = restHighLevelClient.delete(request,RequestOptions.DEFAULT);
            if(Objects.nonNull(response)&&response.status().getStatus()==200){
                logger.info("删除成功");
            }else{
                logger.info("删除失败");
                return false;
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> query(String keyword){
        // 创建查询请求,user是需要查询的索引
        SearchRequest request = new SearchRequest("user");
        // 构建查询参数，比如查询数量，查询耗费时间上限等
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //这里设置超时返回时间，并不是超时停止查询时间，在超时时间内返回结果，但是如果没有查询完的话，还会继续查询。只是不在返回结果
        searchSourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
        //这里是查询返回结果集。es默认返回10条
//        searchSourceBuilder.size(20);
        // 排序，根据id字段排序
        searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.DESC));
        // 查询类型，这里使用查询所有document，使用query进行提交
        MatchQueryBuilder queryBuilder = new MatchQueryBuilder("description",keyword);
        searchSourceBuilder.query(queryBuilder);
        // 将查询参数注入查询请求
        request.source(searchSourceBuilder);
        List<String> results = new ArrayList<>();
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            if (response.status().getStatus() != 200){
                logger.error("查询失败！");
                return results;
            } else {
                logger.info("查询到数量： " + response.getHits().getTotalHits().value);

                for (SearchHit searchHit:response.getHits()){
                    String sourceAsString = searchHit.getSourceAsString();
                    System.out.println("======================================");
                    System.out.println(sourceAsString);
                    results.add(sourceAsString);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}