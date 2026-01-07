package com.erez.ticketbot.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableMongoRepositories(basePackages = "com.erez.ticketbot.inventoryservice.repository")
@EnableTransactionManagement
public class MongoConfig {


    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext context) {

        MappingMongoConverter converter = new MappingMongoConverter( new DefaultDbRefResolver(mongoDbFactory), context);
         converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDbFactory, converter);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }
}