package com.yaologos.searchhouse.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.modelmapper.ModelMapper;
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
public class WebMvcConfig {

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}