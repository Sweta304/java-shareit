package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookStatus;
import ru.practicum.shareit.booking.IncorrectBookingException;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.item.IncorrectCommentException;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentJpaRepository;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.IncorrectOwnerException;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBookerDto;
import static ru.practicum.shareit.item.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.ItemMapper.*;

@Service
public class ItemServiceImpl implements ItemService {

    private ItemJpaRepository itemRepository;
    private UserJpaRepository userRepository;
    private BookingJpaRepository bookingJpaRepository;
    private CommentJpaRepository commentJpaRepository;

    @Autowired
    public ItemServiceImpl(ItemJpaRepository itemRepository, UserJpaRepository userRepository,
                           BookingJpaRepository bookingJpaRepository, CommentJpaRepository commentJpaRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingJpaRepository = bookingJpaRepository;
        this.commentJpaRepository = commentJpaRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long owner) throws UserNotFoundException, ValidationException {
        User user = userRepository.findById(owner).orElseThrow(() -> new UserNotFoundException("Пользователя не существует с id" + owner + "не существует"));
        if (!ItemDto.validateItem(itemDto)) {
            throw new ValidationException("параметры вещи заданы некорректно");
        }
        Item item = fromItemDto(itemDto, user);
        return toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long itemId, Long owner, ItemDto itemDto) throws IncorrectOwnerException, ItemNotFoundException {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Запрашиваемой вещи не существует"));
        if (!(item.getOwner().getId().equals(owner))) {
            throw new IncorrectOwnerException("Вещь не принадлежит указанному пользователю");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithBooking getItem(Long itemId, Long owner) throws ItemNotFoundException {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Запрашиваемой вещи не существует"));
        if (owner.equals(item.getOwner().getId())) {
            return toItemWithBooking(item, findLastBookingForItem(item.getId()), findNextBookingForItem(item.getId()), getCommentsList(commentJpaRepository.findCommentsByItemId(itemId)));
        } else {
            return toItemWithBooking(item, null, null, getCommentsList(commentJpaRepository.findCommentsByItemId(itemId)));
        }
    }

    @Override
    public List<ItemWithBooking> getItems(Long owner) throws UserNotFoundException {
        User user = userRepository.findById(owner).orElseThrow(() -> new UserNotFoundException("Пользователя не существует с id " + owner + " не существует"));
        return itemRepository.findAllByOwner(user)
                .stream()
                .map(x -> toItemWithBooking(x, findLastBookingForItem(x.getId()), findNextBookingForItem(x.getId()), getCommentsList(commentJpaRepository.findCommentsByItemId(x.getId()))))
                .sorted(Comparator.comparing(ItemWithBooking::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        String lowerText = text.toLowerCase();
        return itemRepository.findAll()
                .stream()
                .filter((x -> (x.getName().toLowerCase(new Locale("RU")).contains(lowerText))
                        || (x.getDescription().toLowerCase(new Locale("RU")).contains(lowerText))))
                .filter(x -> x.getAvailable())
                .map(x -> toItemDto(x))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Comment comment, Long itemId, Long owner) throws IncorrectBookingException, IncorrectCommentException {
        List<Booking> bookings = bookingJpaRepository.findByBookerIdAndItemIdAndStatus(owner, itemId, BookStatus.APPROVED);
        if (bookings.isEmpty()) {
            throw new IncorrectBookingException("вы не брали данную вещь в аренду");
        } else if (comment.getText() == null || comment.getText().isBlank() || comment.getText().isEmpty()) {
            throw new IncorrectCommentException("комментарий не может быть пустым!");
        } else if (bookings
                .stream()
                .filter(x -> x.getEnd().isBefore(LocalDateTime.now()))
                .findAny()
                .isEmpty()) {
            throw new IncorrectCommentException("бронирование для этой вещи еще не окончено");
        } else {
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new IncorrectBookingException("проверьте id вещи"));
            User user = userRepository.findById(owner).orElseThrow(() -> new IncorrectBookingException("проверьте id владельца"));
            comment.setItem(item);
            comment.setAuthor(user);
            return toCommentDto(commentJpaRepository.save(comment), user.getName());
        }
    }


    private BookerDto findNextBookingForItem(Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        List<Booking> nextBookings = bookingJpaRepository.findByItemId(itemId)
                .stream()
                .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(Collectors.toList());
        if (!nextBookings.isEmpty()) {
            Booking nextBooking = nextBookings.get(0);
            return toBookerDto(nextBooking, item, nextBooking.getBooker().getId());
        } else {
            return null;
        }
    }

    private BookerDto findLastBookingForItem(Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        List<Booking> lastBookings = bookingJpaRepository.findByItemId(itemId)
                .stream()
                .filter(x -> x.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart)
                        .reversed())
                .collect(Collectors.toList());
        if (!lastBookings.isEmpty()) {
            Booking lastBooking = lastBookings.get(0);
            return toBookerDto(lastBooking, item, lastBooking.getBooker().getId());
        } else {
            return null;
        }
    }

    private List<CommentDto> getCommentsList(List<Comment> comments) {
        List<CommentDto> dtoComments = comments.stream()
                .map(x -> toCommentDto(x, userRepository.findById(x.getAuthor().getId()).get().getName()))
                .collect(Collectors.toList());
        return dtoComments;
    }
}
