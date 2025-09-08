package com.elearning.service.services;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base service class containing common functionality
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Slf4j
public abstract class BaseService {
    
    @Autowired
    protected ModelMapper modelMapper;

    /**
     * Map entity to DTO
     */
    protected <T, U> U mapToDto(T entity, Class<U> dtoClass) {
        if (entity == null) {
            return null;
        }
        return modelMapper.map(entity, dtoClass);
    }

    /**
     * Map DTO to entity
     */
    protected <T, U> U mapToEntity(T dto, Class<U> entityClass) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, entityClass);
    }
}
