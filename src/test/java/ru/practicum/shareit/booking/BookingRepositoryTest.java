package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.utils.MyPageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private BookingJpaRepository bookingJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ItemJpaRepository itemJpaRepository;

    @Autowired
    private ItemRequestJpaRepository itemRequestJpaRepository;

    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Booking booking4;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private ItemRequest request;
    private MyPageable page;
    private Sort sort;

    @BeforeEach
    void beforeEach() {
        user1 = userJpaRepository.save(new User(1L, "user 1", "user1@email"));
        user2 = userJpaRepository.save(new User(2L, "user 2", "user2@email"));
        request = itemRequestJpaRepository.save(new ItemRequest(1L, "description", user1, LocalDateTime.now().minusDays(3), new ArrayList<>()));
        item1 = itemJpaRepository.save(new Item(1L, "item1", "description", true, user1, request));
        item2 = itemJpaRepository.save(new Item(2L, "item2", "description", true, user2, request));
        booking1 = bookingJpaRepository.save(new Booking(1L,
                LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                item1,
                user1,
                BookStatus.WAITING));
        booking2 = bookingJpaRepository.save(new Booking(2L,
                LocalDateTime.of(2022, 10, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                item2,
                user2,
                BookStatus.WAITING));
        booking3 = bookingJpaRepository.save(new Booking(3L,
                LocalDateTime.of(2022, 10, 15, 10, 15),
                LocalDateTime.of(2022, 10, 16, 10, 15),
                item1,
                user2,
                BookStatus.WAITING));
        booking4 = bookingJpaRepository.save(new Booking(4L,
                LocalDateTime.of(2022, 10, 15, 10, 15),
                LocalDateTime.of(2022, 10, 16, 10, 15),
                item1,
                user2,
                BookStatus.WAITING));
        sort = Sort.by(Sort.Direction.DESC, "id");
        page = new MyPageable(0, 1, sort);
    }

    @Test
    void findByBookerIdAndStartIsAfter() {
        List<Booking> bookings = bookingJpaRepository.findByBookerIdAndStartIsAfter(user1.getId(), LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
    }

    @Test
    void findByBookerIdAndStartIsAfterPage() {
        Page<Booking> bookings = bookingJpaRepository.findByBookerIdAndStartIsAfter(user1.getId(), LocalDateTime.now(), page);
        assertNotNull(bookings);
        assertEquals(1, bookings.getTotalElements());
    }

    @Test
    void findByBookerIdAndEndIsBefore() {
        List<Booking> bookings = bookingJpaRepository.findByBookerIdAndEndIsBefore(user2.getId(), LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
    }

    @Test
    void findByBookerIdAndEndIsBeforePage() {
        Page<Booking> bookings = bookingJpaRepository.findByBookerIdAndEndIsBefore(user2.getId(), LocalDateTime.now(), page);
        assertNotNull(bookings);
        assertEquals(2, bookings.getTotalElements());
    }

    @Test
    void findByBookerIdAndCurrentState() {
        List<Booking> bookings = bookingJpaRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(user2.getId(), LocalDateTime.now(), LocalDateTime.now());
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
    }

    @Test
    void findByBookerIdAndCurrentStatePage() {
        Page<Booking> bookings = bookingJpaRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(user2.getId(), LocalDateTime.now(), LocalDateTime.now(), page);
        assertNotNull(bookings);
        assertEquals(1, bookings.getTotalElements());
    }

    @AfterEach
    void afterEach() {
        bookingJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        itemJpaRepository.deleteAll();
        itemRequestJpaRepository.deleteAll();
    }

}
