ALTER TABLE collections ADD COLUMN  deleted_at TIMESTAMP(3) NULL        DEFAULT NULL;
ALTER TABLE collections MODIFY      updated_at TIMESTAMP(3) NOT NULL    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3);
ALTER TABLE grammars    ADD COLUMN  deleted_at TIMESTAMP(3) NULL        DEFAULT NULL;
ALTER TABLE grammars    MODIFY      updated_at TIMESTAMP(3) NOT NULL    DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3);
CREATE INDEX idx_collections_sync ON collections    (user_id, updated_at, id);
CREATE INDEX idx_grammars_sync    ON grammars       (collection_id, updated_at, id);