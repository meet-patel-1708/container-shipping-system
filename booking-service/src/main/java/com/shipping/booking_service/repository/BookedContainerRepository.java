package com.shipping.booking_service.repository;

import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookedContainerRepository extends JpaRepository<BookedContainer, Long> {
    Page<BookedContainer> findByUserId(Long userId, Pageable pageable);
    Page<BookedContainer> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);
    Page<BookedContainer> findByStatus(BookingStatus status, Pageable pageable);
    List<BookedContainer> findByContainer_IdAndStatusIn(Long containerId, Collection<BookingStatus> statuses);
    List<BookedContainer> findByContainer_IdAndStatus(Long containerId, BookingStatus status);
    long countByUserIdAndStatusIn(Long userId, Collection<BookingStatus> statuses);
    long countByUserIdAndStatus(Long userId, BookingStatus status);
    boolean existsByUserIdAndContainer_IdAndStatusIn(Long userId, Long containerId, Collection<BookingStatus> statuses);

    @Query("select count(distinct b.container.id) from BookedContainer b " +
            "where b.userId = :userId and b.status in :statuses")
    long countDistinctContainers(@Param("userId") Long userId,
                                 @Param("statuses") Collection<BookingStatus> statuses);
    List<BookedContainer> findByStatusIn(Collection<BookingStatus> statuses);
    List<BookedContainer> findByUserIdAndStatusIn(Long userId, Collection<BookingStatus> statuses);
    long countByStatusIn(Collection<BookingStatus> statuses);
    long countByStatus(BookingStatus status);

    @Query("select count(distinct b.container.id) from BookedContainer b where b.status in :statuses")
    long countAllDistinctContainers(@Param("statuses") Collection<BookingStatus> statuses);
    Page<BookedContainer> findByOriginPortIdOrDestPortId(Long origin, Long dest, Pageable pageable);
    Page<BookedContainer> findByStatusAndOriginPortIdOrStatusAndDestPortId(
            BookingStatus s1, Long origin, BookingStatus s2, Long dest, Pageable pageable);
    List<BookedContainer> findByStatusAndScheduleAtLessThanEqualAndPortNotifiedFalse(BookingStatus s, LocalDate d);
    List<BookedContainer> findByStatusAndScheduleAtBeforeAndPortNotifiedFalse(BookingStatus status, LocalDateTime before);
}
