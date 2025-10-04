package com.example.demo.service;

import com.example.demo.domain.Board;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    
    private final BoardRepository boardRepository;

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public Board findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public Board save(Board board) {
        return boardRepository.save(board);
    }

    @Transactional
    public void delete(Long id) {
        // 존재 여부 확인
        if (!boardRepository.existsById(id)) {
            throw new ResourceNotFoundException("삭제할 게시글을 찾을 수 없습니다. ID: " + id);
        }
        boardRepository.deleteById(id);
    }
}
