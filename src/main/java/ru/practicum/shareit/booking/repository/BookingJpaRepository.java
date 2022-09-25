package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long owner);
    List<Booking> findByItemId(Long itemId);

    @Query(value = "select * from booking " +
            "where booking_start >= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start asc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId);

    @Query(value = "select * from booking " +
            "where booking_end <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start asc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId);

    @Query(value = "select * from booking " +
            "where booking_end >= CURRENT_TIMESTAMP " +
            "and booking_start <= CURRENT_TIMESTAMP " +
            "and booker_id = :bookerId " +
            "order by booking_start asc",
            nativeQuery = true)
    List<Booking> findByBookerIdAndCurrentState(Long bookerId);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookStatus status);

}