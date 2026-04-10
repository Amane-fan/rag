create extension if not exists vector;

create table if not exists knowledge_base (
    id varchar(64) primary key,
    name varchar(128) not null,
    description varchar(512),
    status varchar(32) not null,
    embedding_model varchar(128),
    top_k integer not null default 4,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists document_record (
    id varchar(64) primary key,
    knowledge_base_id varchar(64) not null,
    file_name varchar(256) not null,
    content_type varchar(128),
    -- Parsed document content is stored in Aliyun OSS; this column stores the OSS object key.
    content_storage_key varchar(512),
    status varchar(32) not null,
    error_message varchar(512),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_document_record_kb on document_record(knowledge_base_id);

create table if not exists document_segment (
    id varchar(64) primary key,
    knowledge_base_id varchar(64) not null,
    document_id varchar(64) not null,
    sequence_no integer not null,
    content text not null,
    created_at timestamp not null
);

create index if not exists idx_document_segment_kb on document_segment(knowledge_base_id);
create index if not exists idx_document_segment_doc on document_segment(document_id);

create table if not exists async_task (
    id varchar(64) primary key,
    business_id varchar(64) not null,
    task_type varchar(32) not null,
    status varchar(32) not null,
    error_message varchar(512),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_async_task_business on async_task(business_id);
