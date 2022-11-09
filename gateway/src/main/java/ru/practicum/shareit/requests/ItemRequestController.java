package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final RequestsClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader("X-Sharer-User-Id") @Positive Long requestor,
                                                 @RequestBody @Valid ItemRequestDto itemRequest) {
        log.info("Creating item request {}, userId={}", itemRequest, requestor);
        return requestClient.addItemRequest(requestor, itemRequest);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestDtos(@RequestHeader("X-Sharer-User-Id") @Positive Long requestor) {
        log.info("Get item requests for user={}", requestor);
        return requestClient.getItemRequestDtos(requestor);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequestDtos(@RequestHeader("X-Sharer-User-Id") @Positive Long requestor,
                                                        @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(required = false, defaultValue = "20") @Positive Integer size) {
        log.info("Get item requests for user={} from={}, size={}", requestor, from, size);
        return requestClient.getAllItemRequestDtos(requestor, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") @Positive Long requestor,
                                                     @PathVariable Long requestId) {
        log.info("Get item request={} fro requestor={}", requestId, requestor);
        return requestClient.getItemRequestById(requestor, requestId);
    }

}
