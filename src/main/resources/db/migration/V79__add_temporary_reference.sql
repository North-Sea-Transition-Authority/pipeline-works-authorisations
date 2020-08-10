ALTER TABLE ${datasource.user}.pad_pipelines
ADD temporary_ref VARCHAR2(4000);

CREATE SEQUENCE ${datasource.user}.pipeline_numbering_seq
  START WITH ${pipelineSequenceStart}
  MINVALUE 1
  MAXVALUE 999999999999999999999999999
  NOCACHE;

