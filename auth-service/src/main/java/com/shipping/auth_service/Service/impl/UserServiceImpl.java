package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.DTO.Request.UpdateUserRequest;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.UserResponse;
import com.shipping.auth_service.Event.UserDeletedEvent;
import com.shipping.auth_service.Event.UserEventPublisher;
import com.shipping.auth_service.Event.UserUpdatedEvent;
import com.shipping.auth_service.Exception.PhoneAlreadyExistsException;
import com.shipping.auth_service.Exception.UserNotFoundException;
import com.shipping.auth_service.Mapper.UserMapper;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Service.UserService;
import com.shipping.auth_service.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final UserEventPublisher publisher;


    @Override
    @Transactional
    @Cacheable(value = "users",key = "#userId")
    public UserResponse getById(Long userId){
        User user = repository.findById(userId).
                orElseThrow(()->new UserNotFoundException(userId));
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    @Cacheable(value = "userLists",key = "#pageable.pageNumber+'-'+#pageable.pageSize")
    public PagedResponse<UserResponse> list(Pageable pageable) {
        Page<UserResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userLists", allEntries = true)
    })
    @Transactional
    public UserResponse update(Long userId, UpdateUserRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(userId));
        if (request.phone() != null
                && !request.phone().equals(user.getPhone())
                && repository.existsByPhone(request.phone())
        ) {
            throw new PhoneAlreadyExistsException(request.phone());
        }
        mapper.toUpdate(request,user);
        User saved = repository.save(user);
        publisher.publishUserUpdated(new UserUpdatedEvent(saved.getUserId(),saved.getName(),saved.getEmail(),saved.getPhone()));
        return mapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = "users",key = "#userId")
    @Transactional
    public void delete(Long userId) {
        if(!repository.existsById(userId)){
            throw new UserNotFoundException(userId);
        }
        repository.deleteById(userId);
        publisher.publishUserDeleted(new UserDeletedEvent(userId));
    }

}
