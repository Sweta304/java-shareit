package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.fromItemDto;

@Component
public class ItemRepositoryImpl implements ItemRepository {

    private Map<Long, Item> items = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long owner) throws ValidationException {
        itemDto.setId(makeId());
        Item item = new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                null);
        items.put(itemDto.getId(), item);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item item = items.get(itemDto.getId());
        items.put(itemDto.getId(), fromItemDto(itemDto, item.getOwner()));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        ItemDto itemDto = ItemMapper.toItemDto(items.get(itemId));
        return itemDto;
    }

    @Override
    public List<ItemDto> getItems(Long owner) {
        List<Item> itemsList = new ArrayList<>(items.values());
        itemsList.stream().map(item -> ItemMapper.toItemDto(item)).collect(Collectors.toList());
        Map<Long, Item> itemsMap = new HashMap<>();
        itemsList.stream().forEach(i -> itemsMap.put(i.getId(), i));
        List<ItemDto> foundItemsDto = itemsList.stream()
                .filter(x -> x.getOwner().equals(owner))
                .map(x -> ItemMapper.toItemDto(x))
                .collect(Collectors.toList());
        return foundItemsDto;
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        String lowerCaseText = text.toLowerCase();
        List<Item> foundItems = items.values().stream()
                .filter(x -> (x.getName().toLowerCase().contains(lowerCaseText) || x.getDescription().toLowerCase().contains(lowerCaseText)) && x.getAvailable())
                .collect(Collectors.toList());
        List<ItemDto> foundItemsDto = foundItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return foundItemsDto;
    }

    @Override
    public Item getItemById(Long id) {
        return items.get(id);
    }

    public Long makeId() {
        return ++lastId;
    }
}
