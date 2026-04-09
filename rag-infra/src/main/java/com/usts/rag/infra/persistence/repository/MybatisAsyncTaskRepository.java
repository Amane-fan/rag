package com.usts.rag.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usts.rag.domain.entity.AsyncTaskEntity;
import com.usts.rag.domain.repository.AsyncTaskRepository;
import com.usts.rag.infra.persistence.mapper.AsyncTaskMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisAsyncTaskRepository implements AsyncTaskRepository {

    private final AsyncTaskMapper asyncTaskMapper;

    public MybatisAsyncTaskRepository(AsyncTaskMapper asyncTaskMapper) {
        this.asyncTaskMapper = asyncTaskMapper;
    }

    @Override
    public void save(AsyncTaskEntity task) {
        asyncTaskMapper.insert(task);
    }

    @Override
    public void update(AsyncTaskEntity task) {
        asyncTaskMapper.updateById(task);
    }

    @Override
    public Optional<AsyncTaskEntity> findById(String id) {
        return Optional.ofNullable(asyncTaskMapper.selectById(id));
    }

    @Override
    public List<AsyncTaskEntity> findAll() {
        return asyncTaskMapper.selectList(new LambdaQueryWrapper<AsyncTaskEntity>()
                .orderByDesc(AsyncTaskEntity::getCreatedAt));
    }
}
