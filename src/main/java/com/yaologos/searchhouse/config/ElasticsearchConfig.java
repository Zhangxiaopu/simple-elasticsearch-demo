package com.yaologos.searchhouse.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * simple-elasticsearch-demo
 * Description:
 * User: 张普
 * Date: 2020-07-24
 * Time: 11:16
 */
@Configuration
public class ElasticsearchConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost",9200,"http"),
                new HttpHost("localhost",9300,"http")
        ));
    }
}
