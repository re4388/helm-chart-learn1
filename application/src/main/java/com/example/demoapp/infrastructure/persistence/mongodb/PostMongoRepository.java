package com.example.demoapp.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data MongoDB repository for PostDocument
 * PostDocument 的 Spring Data MongoDB 儲存庫
 */
@Repository
public interface PostMongoRepository extends MongoRepository<PostDocument, String> {
    
    /**
     * Find posts by status
     * 根據狀態尋找文章
     */
    List<PostDocument> findByStatus(String status);
}