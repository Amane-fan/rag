package com.usts.rag.rag.service;

import com.usts.rag.common.exception.BusinessException;
import com.usts.rag.common.exception.ErrorCode;
import com.usts.rag.domain.entity.AsyncTaskEntity;
import com.usts.rag.domain.repository.AsyncTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskApplicationService {

    private final AsyncTaskRepository asyncTaskRepository;

    public TaskApplicationService(AsyncTaskRepository asyncTaskRepository) {
        this.asyncTaskRepository = asyncTaskRepository;
    }

    public List<AsyncTaskEntity> list() {
        return asyncTaskRepository.findAll();
    }

    public AsyncTaskEntity get(String id) {
        return asyncTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Task not found"));
    }
}
