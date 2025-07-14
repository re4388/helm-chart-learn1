package com.example.demoapp.infrastructure.persistence.mongodb;

import com.example.demoapp.domain.model.Post;
import com.example.demoapp.domain.port.PostRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of PostRepository
 * PostRepository 的 MongoDB 實作
 */
@Repository
public class PostRepositoryImpl implements PostRepository {
    
    private final PostMongoRepository mongoRepository;
    
    public PostRepositoryImpl(PostMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }
    
    @Override
    public Post save(Post post) {
        PostDocument document = PostDocument.fromDomain(post);
        PostDocument savedDocument = mongoRepository.save(document);
        return savedDocument.toDomain();
    }
    
    @Override
    public Optional<Post> findById(String id) {
        Optional<PostDocument> byId = mongoRepository.findById(id);
        System.out.println(byId);
        return byId.map(PostDocument::toDomain);
    }
    
    @Override
    public List<Post> findAll() {
        return mongoRepository.findAll()
            .stream()
            .map(PostDocument::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Post> findByStatus(Post.PostStatus status) {
        return mongoRepository.findByStatus(status.name())
            .stream()
            .map(PostDocument::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return mongoRepository.existsById(id);
    }
}