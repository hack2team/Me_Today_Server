-- V7: Update ai_analysis table to support comprehensive AI analysis
-- Drop old path_to_ideal column and add new fields for structured analysis

ALTER TABLE ai_analysis DROP COLUMN path_to_ideal;
ALTER TABLE ai_analysis ADD COLUMN `values` TEXT NULL COMMENT '사용자 가치관';
ALTER TABLE ai_analysis ADD COLUMN improvement_suggestions TEXT NULL COMMENT '개선 제안사항';
