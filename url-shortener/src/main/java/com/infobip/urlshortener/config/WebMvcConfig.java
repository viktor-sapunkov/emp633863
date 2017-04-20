package com.infobip.urlshortener.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        final ObjectMapper objectMapper = this.buildObjectMapper();
        objectMapper.registerModule(this.getModule());
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
        super.extendMessageConverters(converters);
    }

    public ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private SimpleModule getModule() {
        // Register custom serializers
        SimpleModule module = new SimpleModule("DefaultModule");

        return module;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1 year cache period
        registry.addResourceHandler("/help.html").addResourceLocations("/help.html").setCachePeriod(31556926);
        registry.addResourceHandler("/favicon", "/help", "/css", "/img", "/js").addResourceLocations("/")
                .setCachePeriod(31556926);

        registry.setOrder(-1);
    }

}
