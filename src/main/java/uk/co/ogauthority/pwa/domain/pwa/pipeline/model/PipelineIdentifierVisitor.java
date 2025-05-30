package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

/**
 * Interface which allows custom behaviour to be added to PipelineIdentifer implementing classes without modifying the
 * implementations themselves.
 * As with all visitor patterns, any new classes implementing {@link PipelineIdentifier} should be added here and the
 * relevant visitor classes updated.
 */
public interface PipelineIdentifierVisitor {

  void visit(PipelineId pipelineId);

  void visit(PipelineSection pipelineSection);

}
