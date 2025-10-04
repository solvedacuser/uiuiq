package com.example.demo.dto.request;

import com.example.demo.domain.Board;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시판 등록/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequestDto {
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    private String author;
    
    /**
     * DTO를 Entity로 변환
     */
    public Board toEntity() {
        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setAuthor(author);
        return board;
    }
}

