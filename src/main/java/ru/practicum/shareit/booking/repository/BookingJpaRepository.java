package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdAndItemIdAndStatus(Long bookerId, Long itemId, BookStatus status);

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start);

    Page<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end);

    Page<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookStatus status, Sort sort);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookStatus status, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

}
