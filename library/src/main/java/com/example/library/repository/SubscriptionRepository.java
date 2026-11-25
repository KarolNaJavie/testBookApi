package com.example.library.repository;

import com.example.library.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    boolean existsByClientIdAndAuthor(Long clientId, String author);

    boolean existsByClientIdAndCategory(Long clientId, String category);

    void deleteByClientIdAndAuthor(Long clientId, String author);

    void deleteByClientIdAndCategory(Long clientId, String category);

    List<Subscription> findByAuthorOrCategory(String author, String category);

    @Query("""
    SELECT s FROM Subscription s
    WHERE (:author IS NOT NULL AND s.author = :author)
       OR (:category IS NOT NULL AND s.category = :category)
""")
    List<Subscription> findMatchingSubscriptions(
            @Param("author") String author,
            @Param("category") String category
    );
}