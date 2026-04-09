package com.usts.rag.infra.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usts.rag.domain.entity.KnowledgeBaseEntity;
import com.usts.rag.domain.repository.KnowledgeBaseRepository;
import com.usts.rag.infra.persistence.mapper.KnowledgeBaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisKnowledgeBaseRepository implements KnowledgeBaseRepository {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public MybatisKnowledgeBaseRepository(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public void save(KnowledgeBaseEntity knowledgeBase) {
        knowledgeBaseMapper.insert(knowledgeBase);
    }

    @Override
    public void update(KnowledgeBaseEntity knowledgeBase) {
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    @Override
    public Optional<KnowledgeBaseEntity> findById(String id) {
        return Optional.ofNullable(knowledgeBaseMapper.selectById(id));
    }

    @Override
    public List<KnowledgeBaseEntity> findAll() {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBaseEntity>()
                .orderByDesc(KnowledgeBaseEntity::getCreatedAt));
    }
}
