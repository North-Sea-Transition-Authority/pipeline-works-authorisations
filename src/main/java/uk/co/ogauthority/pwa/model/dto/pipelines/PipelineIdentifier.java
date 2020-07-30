package uk.co.ogauthority.pwa.model.dto.pipelines;

/* To be implemented by classes that represent some or all of a pipeline */
public interface PipelineIdentifier {

  PipelineId getPipelineId();

  int getPipelineIdAsInt();

  /**
   * <p>Accept a visitor who will interpret the pipeline identifier to produce a string for display purposes.
   * Implement as follows to use double dispatch so the appropriate method on the visitor is run.</p>
   *
   * <code>void accept(PipelineIdentifierDisplayNameVisitor visitor){visitor.visit(this);}</code>
   *
   */
  void accept(PipelineIdentifierVisitor visitor);

}
