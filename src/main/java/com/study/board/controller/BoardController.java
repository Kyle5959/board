package com.study.board.controller;

import com.study.board.entity.Board;
import com.study.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller

public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/board/write") //localhost:8080/board/write
    public String BoardWriteForm() {
        return "boardwrite";
    }

    @PostMapping("/board/writepro")
    public String BoardWritePro(Board board, Model model, MultipartFile file) throws Exception {

        boardService.write(board, file);

        model.addAttribute("message", "글 작성이 완료되었습니다.");
        model.addAttribute("searchUrl", "/board/list");

        return "message";
    }

    @GetMapping("/board/list")
    public String boardlist(Model model,
                            @PageableDefault(page = 0, size = 10, sort = "id",
                            direction = Sort.Direction.DESC) Pageable pageable,
                            String searchKeyword) {

        Page<Board> list = null;

        if(searchKeyword == null) {
            list = boardService.boardList(pageable);
        } else {
            list = boardService.boardSearchList(searchKeyword, pageable);
        }

        int nowPage = list.getPageable().getPageNumber() + 1;
        int startPage = Math.max(nowPage - 4, 1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

                model.addAttribute("list", list);
                model.addAttribute("nowPage", nowPage);
                model.addAttribute("startPage", startPage);
                model.addAttribute("endPage", endPage);

        return "boardlist";
    }

    @GetMapping("/board/view") // localhost:8080/board/view?id=1
    public String boardView(Model model, Integer id){

        model.addAttribute("board", boardService.boardView(id));
        return "boardview";
    }

    @GetMapping("/board/delete")
    public String boardDelete(Integer id){

        boardService.boardDelete(id);

        return "redirect:/board/list";

    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id, Model model){

        model.addAttribute("board", boardService.boardView(id));

        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board, Model model, MultipartFile file) throws Exception{

        Board boardTemp = boardService.boardView(id);

        // 파일 업데이트 로직 (매우 중요!)
        // 1. 새로운 파일이 첨부된 경우: 새 파일 저장 및 DB 정보 업데이트
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제 로직 (선택 사항, 필요하다면 추가)
            // if (boardTemp.getFilename() != null) { ... 기존 파일 삭제 로직 ... }

            // BoardService의 write 메서드는 파일 저장 및 board.setFilename/setFilepath를 처리하므로,
            // boardTemp 객체에 새로운 파일 정보가 잘 설정될 것입니다.
            boardService.write(boardTemp, file); // boardTemp 객체에 새 파일 정보가 바인딩됨
        } else {
            // 2. 새로운 파일이 첨부되지 않은 경우: 기존 파일 정보를 유지
            // (BoardService의 write 메서드에서 file이 null일 때 board.setFilename/setFilepath를 건드리지 않도록 로직을 추가해야 합니다.)
            // 즉, boardTemp에 기존 파일 정보가 그대로 유지되도록 처리해야 합니다.
        }

        // 제목과 내용은 항상 업데이트
        boardTemp.setTitle(board.getTitle());
        boardTemp.setContent(board.getContent());

        // DB에 최종 업데이트된 boardTemp 저장 (파일 정보도 포함)
        // write 메서드 내부에 save 로직이 있으므로 이 부분은 필요 없을 수도 있습니다.
        // boardService.save(boardTemp); // (만약 BoardService에 별도의 save 메서드가 있다면)

        // BoardService.write(boardTemp, file); 호출 시 이미 save가 되므로,
        // 위에서 boardService.write(boardTemp, file); 호출 후에는 따로 save를 호출할 필요 없습니다.
        // 다만, BoardService.write 메서드가 "새 글 작성"과 "글 수정+파일 처리"를 모두 담당하므로,
        // 이 부분을 write 메서드에서 더 잘 분리하는 것이 좋습니다.
        // (예: BoardService.updateBoard(Board board, MultipartFile file, boolean isNewFileIncluded))

        model.addAttribute("message", "수정이 완료되었습니다.");
        model.addAttribute("searchUrl", "/board/view?id=" + boardTemp.getId());

        return "message";

    }

}

