package com.elearning.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface with common operations
 * 
 * @param <T> Entity type
 * @param <ID> Primary key type
 * @author Your Name
 * @version 1.0.0
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    // Common repository methods can be added here
}
