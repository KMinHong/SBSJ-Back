package com.example.sbsj_process.board.service.freeBoardService;

import com.example.sbsj_process.board.entity.free.FreeBoard;
import com.example.sbsj_process.board.service.request.FreeBoardModifyRequest;
import com.example.sbsj_process.board.service.request.FreeBoardRegisterRequest;
import com.example.sbsj_process.board.service.response.FreeBoardListResponse;

import java.util.List;

public interface FreeBoardService {

    List<FreeBoardListResponse> list();


}
