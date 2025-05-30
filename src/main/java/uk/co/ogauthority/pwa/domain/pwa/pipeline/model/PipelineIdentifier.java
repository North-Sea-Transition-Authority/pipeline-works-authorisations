package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

/* To be implemented by classes that represent some or all of a pipeline */
public interface PipelineIdentifier {

  PipelineId getPipelineId();

  int getPipelineIdAsInt();

  /**
   * Accept a visitor who will interpret the pipeline identifier to produce a string for display purposes.
   * Implement as follows to use double dispatch so the appropriate method on the visitor is run.
   *
   * <code>void accept(PipelineIdentifierDisplayNameVisitor visitor){visitor.visit(this);}</code>
   *
   */
  void accept(PipelineIdentifierVisitor visitor);

}
