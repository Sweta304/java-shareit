package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwner(User owner);

    Page<Item> findAllByOwner(User owner, Pageable pageable);

    List<Item> findItemsByRequestId(Long requestId);

    Page<Item> findAll(Pageable pageable);

}
