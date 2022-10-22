package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdAndItemIdAndStatus(Long bookerId, Long itemId, BookStatus status);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByItemId(Long itemId);

    @Query(value = "select * from booking " +
            "where booking_start >= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId);

    @Query(value = "select * from booking " +
            "where booking_start >= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    Page<Booking> findByBookerIdAndStartIsAfter(Long bookerId, Pageable pageable);

    @Query(value = "select * from booking " +
            "where booking_end <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId);

    @Query(value = "select * from booking " +
            "where booking_end <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    Page<Booking> findByBookerIdAndEndIsBefore(Long bookerId, Pageable pageable);

    @Query(value = "select * from booking " +
            "where booking_end >= CURRENT_TIMESTAMP " +
            "and booking_start <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndCurrentState(Long bookerId);

    @Query(value = "select * from booking " +
            "where booking_end >= CURRENT_TIMESTAMP " +
            "and booking_start <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start desc",
            nativeQuery = true)
    Page<Booking> findByBookerIdAndCurrentState(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookStatus status, Sort sort);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookStatus status, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

}
