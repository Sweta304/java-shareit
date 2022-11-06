package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentJpaRepository;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.requests.RequestNotFoundException;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestJpaRepository;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;
import ru.practicum.shareit.utils.MyPageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingMapper.toBookerDto;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;
import static ru.practicum.shareit.item.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.ItemMapper.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemServiceTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemJpaRepository itemRepository;
    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private BookingJpaRepository bookingJpaRepository;
    @Mock
    private CommentJpaRepository commentJpaRepository;
    @Mock
    private ItemRequestJpaRepository itemRequestJpaRepository;
    private BookingIncomingDto bookingIncomingDto;
    private BookingDto bookingDto;
    private Booking booking;
    private Booking secondBooking;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private User owner;
    private BookStatus bookStatus;
    private ItemRequest itemRequest;
    private MyPageable page;
    private Sort sort;
    private BookerDto last;
    private BookerDto next;
    private ItemWithBooking itemWithBooking;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void beforeEach() {
        bookingIncomingDto = new BookingIncomingDto(LocalDateTime.of(2022, 11, 15, 10, 15),
                LocalDateTime.of(2022, 12, 15, 10, 15),
                1L);
        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        user = new User();
        user.setId(1L);
        user.setName("name");
        owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        item.setOwner(owner);
        bookStatus = BookStatus.APPROVED;
        booking = new Booking(1L,
                LocalDateTime.of(2022, 8, 15, 10, 15),
                LocalDateTime.of(2022, 8, 15, 10, 15),
                item,
                user,
                bookStatus);
        secondBooking = new Booking(2L,
                LocalDateTime.of(2022, 11, 16, 10, 15),
                LocalDateTime.of(2022, 12, 16, 10, 15),
                item,
                user,
                BookStatus.WAITING);
        bookingDto = toBookingDto(booking);
        itemDto = new ItemDto("item1", "description", true, 1L, 1L);
        sort = Sort.by(Sort.Direction.DESC, "start");
        page = new MyPageable(0, 2, sort);
        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        item = fromItemDto(itemDto, user, itemRequest);
        comment = new Comment(1L, "text", item, user);
        commentDto = toCommentDto(comment, user.getName());
        last = toBookerDto(booking, item, user.getId());
        next = toBookerDto(secondBooking, item, owner.getId());
        itemWithBooking = toItemWithBooking(item, last, next, List.of(commentDto));
    }

    @AfterEach
    void afterEach() {
        item.setId(1L);
        item.setAvailable(true);
        user.setId(1L);
        owner.setId(2L);
        item.setOwner(owner);
        booking.setStatus(BookStatus.APPROVED);
        bookingDto.setStatus(BookStatus.APPROVED);
        bookingIncomingDto.setStart(LocalDateTime.of(2022, 11, 15, 10, 15));
    }

    @Test
    void addItemWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemService.addItem(itemDto, 1L));
    }

    @Test
    void addItemNotValid() {
        itemDto.setDescription(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> itemService.addItem(itemDto, 1L));
        itemDto.setDescription("description");
    }

    @Test
    void addItemRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RequestNotFoundException.class, () -> itemService.addItem(itemDto, 1L));
    }

    @Test
    void addItem() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestJpaRepository.findById(itemDto.getRequestId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any())).thenReturn(item);
        assertEquals(itemDto, itemService.addItem(itemDto, 1L));
    }

    @Test
    void updateItemNotFound() {
        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(1L, 1L, itemDto));
    }

    @Test
    void updateItemIncorrectOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(IncorrectOwnerException.class, () -> itemService.updateItem(1L, 2L, itemDto));
    }

    @Test
    void updateItemName() throws Exception {
        itemDto.setDescription(null);
        itemDto.setAvailable(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        assertEquals(toItemDto(item), itemService.updateItem(1L, 1L, itemDto));
        itemDto.setDescription("description");
        itemDto.setAvailable(true);
    }

    @Test
    void updateItemDescription() throws Exception {
        itemDto.setName(null);
        itemDto.setAvailable(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        assertEquals(toItemDto(item), itemService.updateItem(1L, 1L, itemDto));
        itemDto.setAvailable(true);
        itemDto.setName("item1");
    }

    @Test
    void updateItemAvailable() throws Exception {
        itemDto.setName(null);
        itemDto.setDescription(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        assertEquals(toItemDto(item), itemService.updateItem(1L, 1L, itemDto));
        itemDto.setDescription("description");
        itemDto.setName("item1");
    }

    @Test
    void getItemNotFound() {
        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(1L, 1L));
    }

    @Test
    void getItemOwner() throws Exception {
        itemWithBooking.setNextBooking(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingJpaRepository.findByItemId(1L)).thenReturn(List.of(booking));
        when(commentJpaRepository.findCommentsByItemId(anyLong())).thenReturn(List.of(comment));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        ItemWithBooking itemDto1 = itemService.getItem(1L, 1L);
        assertEquals(itemWithBooking.getId(), itemService.getItem(1L, 1L).getId());
        assertEquals(itemWithBooking.getLastBooking(), itemService.getItem(1L, 1L).getLastBooking());
        assertEquals(itemWithBooking.getNextBooking(), itemService.getItem(1L, 1L).getNextBooking());
        assertEquals(itemWithBooking.getComments().size(), itemService.getItem(1L, 1L).getComments().size());
        itemWithBooking.setNextBooking(next);
    }

    @Test
    void getItemNotOwner() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingJpaRepository.findByItemId(1L)).thenReturn(List.of(booking));
        when(commentJpaRepository.findCommentsByItemId(anyLong())).thenReturn(List.of(comment));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        ItemWithBooking itemDto1 = itemService.getItem(1L, 2L);
        assertEquals(itemWithBooking.getId(), itemDto1.getId());
        assertNull(itemDto1.getLastBooking());
        assertNull(itemDto1.getNextBooking());
        assertEquals(itemWithBooking.getComments().size(), itemDto1.getComments().size());
    }

    @Test
    void getItemsUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> itemService.getItems(1L, null, null));
    }

    @Test
    void getItemsPagination() throws Exception {
        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = new MyPageable(0, 1, sortByCreated);
        Page<Item> itemsPage = new PageImpl<>(List.of(item));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingJpaRepository.findByItemId(1L)).thenReturn(List.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findAllByOwner(user, pageable)).thenReturn(itemsPage);
        when(commentJpaRepository.findCommentsByItemId(anyLong())).thenReturn(List.of(comment));
        List<ItemWithBooking> items = itemService.getItems(user.getId(), 0, 1);
        assertEquals(1, items.size());
        assertEquals(itemWithBooking.getId(), items.get(0).getId());
        assertEquals(itemWithBooking.getLastBooking(), items.get(0).getLastBooking());
        assertNull(items.get(0).getNextBooking());
    }

    @Test
    void getItemsWithoutPagination() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingJpaRepository.findByItemId(1L)).thenReturn(List.of(booking));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(commentJpaRepository.findCommentsByItemId(anyLong())).thenReturn(List.of(comment));
        when(itemRepository.findAllByOwner(user)).thenReturn(List.of(item));
        List<ItemWithBooking> items = itemService.getItems(user.getId(), null, null);
        assertEquals(1, items.size());
        assertEquals(itemWithBooking.getId(), items.get(0).getId());
        assertEquals(itemWithBooking.getLastBooking(), items.get(0).getLastBooking());
        assertNull(items.get(0).getNextBooking());
    }

    @Test
    void searchItemEmptyText() throws Exception {
        assertEquals(new ArrayList<>(), itemService.searchItem("", 0, 3));
    }

    @Test
    void searchItemPagination() throws Exception {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = new MyPageable(0, 1, sortById);
        Page<Item> itemsPage = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(pageable)).thenReturn(itemsPage);
        List<ItemDto> items = itemService.searchItem("description", 0, 1);
        assertEquals(toItemDto(item), items.get(0));
        assertEquals(1, items.size());
    }

    @Test
    void addCommentEmptyBookings() {
        when(bookingJpaRepository.findByBookerIdAndItemIdAndStatus(1L, 1L, BookStatus.APPROVED)).thenReturn(new ArrayList<>());
        assertThrows(IncorrectBookingException.class, () -> itemService.addComment(commentDto, 1L, 1L));
    }

    @Test
    void addCommentTextEmpty() {
        when(bookingJpaRepository.findByBookerIdAndItemIdAndStatus(1L, 1L, BookStatus.APPROVED)).thenReturn(List.of(booking));
        comment.setText("");
        assertThrows(IncorrectCommentException.class, () -> itemService.addComment(commentDto, 1L, 1L));
        comment.setText("text");
    }

    @Test
    void addCommentBookingNotFinished() {
        when(bookingJpaRepository.findByBookerIdAndItemIdAndStatus(1L, 1L, BookStatus.APPROVED)).thenReturn(List.of(secondBooking));
        assertThrows(IncorrectCommentException.class, () -> itemService.addComment(commentDto, 1L, 1L));
    }

    @Test
    void addCommentItemNotFound() {
        when(bookingJpaRepository.findByBookerIdAndItemIdAndStatus(1L, 1L, BookStatus.APPROVED)).thenReturn(List.of(booking));
        assertThrows(IncorrectBookingException.class, () -> itemService.addComment(commentDto, 1L, 1L));
    }

    @Test
    void addCommentUserNotFound() throws Exception {
        when(bookingJpaRepository.findByBookerIdAndItemIdAndStatus(1L, 1L, BookStatus.APPROVED)).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentJpaRepository.save(comment)).thenReturn(comment);
        CommentDto actual = itemService.addComment(commentDto, 1L, 1L);
        assertEquals(commentDto.getId(), actual.getId());
        assertEquals(commentDto.getText(), actual.getText());
        assertEquals(commentDto.getAuthorName(), actual.getAuthorName());
    }

}
