package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwner(Long owner);

}
