package com.study.board.service;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    //글 작성
    public void write(Board board, MultipartFile file) throws Exception {

        String projectPath = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "main" +
                File.separator + "resources" +
                File.separator + "static" +
                File.separator + "files";

        File directory = new File(projectPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (file != null && !file.isEmpty()) { // 파일이 존재하고 비어있지 않을 경우에만 파일 처리 및 저장
            UUID uuid = UUID.randomUUID();
            String filename = uuid + "_" + file.getOriginalFilename();

            File saveFile = new File(projectPath, filename);
            file.transferTo(saveFile);

            // DB에 파일 이름과 경로 저장
            board.setFilename(filename); // <-- Board 엔티티에 파일 이름 설정
            board.setFilepath("/files/" + filename); // <-- Board 엔티티에 웹 접근 경로 설정
        } else {
            // 파일이 첨부되지 않았을 경우, 기존 파일 정보를 유지하거나 (수정 시)
            // 새로 작성할 경우 비워둡니다.
            // 수정 시 기존 파일 정보를 유지하려면, boardTemp에 기존 파일 이름을 가져와서
            // setFilename, setFilepath 해줘야 합니다. (아래 3번 참고)
        }

        boardRepository.save(board);

    }

    // 게시글 리스트 처리
    public Page<Board> boardList(Pageable pageable){

        return boardRepository.findAll(pageable);
    }

    // 특정 게시글 불러오기
    public Board boardView(Integer id){

        return boardRepository.findById(id).get();
    }

    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {

        return boardRepository.findByTitleContaining(searchKeyword, pageable);

    }

    // 특정 게시글 삭제
    public void boardDelete(Integer id){

        boardRepository.deleteById(id);

    }

}
