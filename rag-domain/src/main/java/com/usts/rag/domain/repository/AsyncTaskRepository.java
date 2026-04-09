package com.usts.rag.domain.repository;

import com.usts.rag.domain.entity.AsyncTaskEntity;

import java.util.List;
import java.util.Optional;

public interface AsyncTaskRepository {

    void save(AsyncTaskEntity task);

    void update(AsyncTaskEntity task);

    Optional<AsyncTaskEntity> findById(String id);

    List<AsyncTaskEntity> findAll();
}
