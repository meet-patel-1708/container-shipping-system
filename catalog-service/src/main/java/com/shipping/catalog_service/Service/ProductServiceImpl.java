package com.shipping.catalog_service.Service;

import com.shipping.catalog_service.DTO.PagedResponse;
import com.shipping.catalog_service.DTO.ProductRequest;
import com.shipping.catalog_service.DTO.ProductResponse;
import com.shipping.catalog_service.Entity.Product;
import com.shipping.catalog_service.Event.ProductCreatedEvent;
import com.shipping.catalog_service.Event.ProductDeletedEvent;
import com.shipping.catalog_service.Event.ProductEventPublisher;
import com.shipping.catalog_service.Event.ProductUpdatedEvent;
import com.shipping.catalog_service.Exception.ProductCodeAlreadyExistException;
import com.shipping.catalog_service.Exception.ProductNotFoundException;
import com.shipping.catalog_service.Mapper.ProductMapper;
import com.shipping.catalog_service.Repository.ProductRepository;
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
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ProductEventPublisher publisher;

    @Override
    @Transactional
    @CacheEvict(value = "productList", allEntries = true)
    public ProductResponse create(Long ownerId, ProductRequest request) {
        if (repository.existsByProductCodeAndOwnerId(request.productCode(), ownerId)) {
            throw new ProductCodeAlreadyExistException(request.productCode());
        }
        Product product = mapper.toEntity(request);
        product.setOwnerId(ownerId);
        Product saved = repository.save(product);
        publisher.publishProductCreated(new ProductCreatedEvent(
                saved.getId(), saved.getOwnerId(), saved.getProductCode(), saved.getProductName(),
                saved.getProductDescription(), saved.getProductPrice(), saved.getProductWeight()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Cacheable(value = "products",      key = "#ownerId + ':' + #productId")
    public ProductResponse getById(Long ownerId, Long productId) {
        // Another user's product returns 404, so its existence isn't leaked
        Product p = repository.findByIdAndOwnerId(productId, ownerId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapper.toResponse(p);
    }

    @Override
    @Transactional
    @Cacheable(value = "productByCode", key = "#ownerId + ':' + #productCode")
    public ProductResponse getByCode(Long ownerId, String productCode) {
        // not cached: the "code already taken" check must always be fresh
        Product p = repository.findByProductCodeAndOwnerId(productCode, ownerId)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        return mapper.toResponse(p);
    }

    @Override
    @Transactional
    @Cacheable(value = "productList",   key = "#ownerId + ':' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<ProductResponse> list(Long ownerId, Pageable pageable) {
        return PagedResponse.from(repository.findByOwnerId(ownerId, pageable).map(mapper::toResponse));
    }

    @Override
    @Transactional
    public PagedResponse<ProductResponse> searchByName(Long ownerId, String name, Pageable pageable) {
        return PagedResponse.from(
                repository.findByOwnerIdAndProductNameContainingIgnoreCase(ownerId, name, pageable)
                        .map(mapper::toResponse));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products",     key = "#ownerId + ':' + #productId")
    public ProductResponse update(Long ownerId, Long productId, ProductRequest request) {
        Product product = repository.findByIdAndOwnerId(productId, ownerId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        mapper.toUpdate(request, product);
        Product saved = repository.save(product);
        publisher.publishProductCreated(new ProductCreatedEvent(
                saved.getId(), saved.getOwnerId(), saved.getProductCode(), saved.getProductName(),
                saved.getProductDescription(), saved.getProductPrice(), saved.getProductWeight()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products",     key = "#ownerId + ':' + #productId")
    public void delete(Long ownerId, Long productId) {
        Product product = repository.findByIdAndOwnerId(productId, ownerId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        repository.delete(product);
        publisher.publishProductDeleted(new ProductDeletedEvent(productId, ownerId));
    }
}
