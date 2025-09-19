package com.example.demo.controller;

import com.example.demo.domain.Board;
import com.example.demo.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("boards", boardService.findAll());
        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "board/writeForm";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute Board board) {
        boardService.save(board);
        return "redirect:/board/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "board/view";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "board/editForm";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Board board) {
        Board existingBoard = boardService.findById(id);
        existingBoard.setTitle(board.getTitle());
        existingBoard.setContent(board.getContent());
        boardService.save(existingBoard);
        return "redirect:/board/view/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board/list";
    }
}
