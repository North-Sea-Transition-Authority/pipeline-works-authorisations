package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierVisitor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;

/**
 * Visitor to PipelineIdentifiers which can map the pipelineIdentifier to a user displayable reference.
 * Greedy algorithm as it relies on a pre-computed map of PipelineId to pipelineNumber string in order work.
 */
// TODO PWA-673 is this over complicated?
public class PipelineIdentifierDisplayNameVisitor implements PipelineIdentifierVisitor {

  // pre-computed map that must contain all PipelineIds that the visitor could visit
  private final Map<PipelineId, String> pipelineIdStringLookup;

  // Added to while visiting map visited identifiers to a suitable display string
  private final Map<PipelineIdentifier, String> pipelineIdentifierStringLookup;

  public PipelineIdentifierDisplayNameVisitor(Map<PipelineId, String> pipelineIdStringLookup) {
    this.pipelineIdStringLookup = pipelineIdStringLookup;
    // seed visitor result map with original map converted into pipelineIdentifier representation
    pipelineIdentifierStringLookup = pipelineIdStringLookup.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void visit(PipelineId pipelineId) {
    pipelineIdentifierStringLookup.put(pipelineId, pipelineIdStringLookup.get(pipelineId));
  }

  @Override
  public void visit(PipelineSegment pipelineSegment) {
    pipelineIdentifierStringLookup.put(pipelineSegment,
        String.format(
            "%s (%s)",
            pipelineIdStringLookup.get(pipelineSegment.getPipelineId()),
            pipelineSegment.getDisplayString()
        )
    );

  }

  public Map<PipelineIdentifier, String> getPipelineIdentifierStringLookup() {
    return Collections.unmodifiableMap(pipelineIdentifierStringLookup);
  }
}
