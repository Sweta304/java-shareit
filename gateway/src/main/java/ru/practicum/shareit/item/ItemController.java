package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectOwnerException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                          @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, owner);
        return itemClient.addItem(owner, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid ItemDto itemDto) throws IncorrectOwnerException, ItemNotFoundException {
        log.info("Updated item {}, userId={}", itemDto, owner);
        return itemClient.updateItem(itemId, owner, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                          @PathVariable Long itemId) {
        log.info("Get itemId={}, userId={}", itemId, owner);
        return itemClient.getItem(itemId, owner);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                           @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                           @RequestParam(required = false, defaultValue = "20") @Positive Integer size) {
        log.info("Get items with for userId={}, from={}, size={}", owner, from, size);
        return itemClient.getItems(owner, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                             @RequestParam String text,
                                             @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(required = false, defaultValue = "20") @Positive Integer size) {
        log.info("Search text={} from={}, size={}", text, from, size);
        return itemClient.searchItem(owner, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") @Positive Long owner,
                                             @RequestBody @Valid CommentDto comment,
                                             @PathVariable Long itemId) {
        return itemClient.addComment(owner, comment, itemId);
    }
}
