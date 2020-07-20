package uk.co.ogauthority.pwa.model.dto.pipelines;

import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineIdentifierDisplayNameVisitor;

public interface PipelineDisplayStringLookupDestination {

  String acceptVisitor(PipelineIdentifierDisplayNameVisitor pipelineIdentifierDisplayNameGreedyVisitor);
}
