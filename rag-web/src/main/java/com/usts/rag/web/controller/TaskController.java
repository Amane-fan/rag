package com.usts.rag.web.controller;

import com.usts.rag.common.api.ApiResponse;
import com.usts.rag.domain.entity.AsyncTaskEntity;
import com.usts.rag.rag.service.TaskApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @GetMapping
    public ApiResponse<List<AsyncTaskEntity>> list() {
        return ApiResponse.success(taskApplicationService.list());
    }

    @GetMapping("/detail")
    public ApiResponse<AsyncTaskEntity> detail(@RequestParam("id") String id) {
        return ApiResponse.success(taskApplicationService.get(id));
    }
}
