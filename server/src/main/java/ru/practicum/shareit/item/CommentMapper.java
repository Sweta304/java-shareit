package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Data
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, String authorName) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                authorName,
                LocalDateTime.now()
        );
    }

    public static Comment fromCommentDto(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        return comment;
    }
}
