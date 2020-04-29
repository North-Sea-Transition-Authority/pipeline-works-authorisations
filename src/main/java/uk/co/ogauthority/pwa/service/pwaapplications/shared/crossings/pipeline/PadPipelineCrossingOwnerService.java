package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingOwnerRepository;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineCrossingOwnerService {

  private final PadPipelineCrossingOwnerRepository padPipelineCrossingOwnerRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PadPipelineCrossingOwnerService(
      PadPipelineCrossingOwnerRepository padPipelineCrossingOwnerRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padPipelineCrossingOwnerRepository = padPipelineCrossingOwnerRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  public List<PadPipelineCrossingOwner> getOwnersForCrossing(PadPipelineCrossing padPipelineCrossing) {
    return padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing);
  }

  public Map<String, String> getOwnerPrepopulationAttribute(PadPipelineCrossing padPipelineCrossing) {
    var owners = getOwnersForCrossing(padPipelineCrossing);
    return owners.stream()
        .collect(StreamUtils.toLinkedHashMap((PadPipelineCrossingOwner owner) -> {
              if (owner.isManualEntry()) {
                return SearchSelectable.FREE_TEXT_PREFIX + owner.getManualOrganisationEntry();
              }
              return String.valueOf(owner.getOrganisationUnit().getOuId());
            }, owner -> owner.isManualEntry()
                ? owner.getManualOrganisationEntry()
                : owner.getOrganisationUnit().getName()
        ));
  }

  public void createOwners(PadPipelineCrossing pipelineCrossing, PipelineCrossingForm form) {
    var existingOwners = padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(pipelineCrossing);
    padPipelineCrossingOwnerRepository.deleteAll(existingOwners);
    if (!BooleanUtils.isTrue(form.getPipelineFullyOwnedByOrganisation())) {
      var owners = form.getPipelineOwners();
      var linkedEntries = owners.stream()
          .filter(s -> !s.startsWith(SearchSelectable.FREE_TEXT_PREFIX))
          .map(Integer::parseInt)
          .collect(Collectors.toList());
      var manualEntries = owners.stream()
          .filter(s -> s.startsWith(SearchSelectable.FREE_TEXT_PREFIX))
          .collect(Collectors.toList());

      createLinkedOwner(pipelineCrossing, linkedEntries);
      createManualOwner(pipelineCrossing, manualEntries);
    }
  }

  @VisibleForTesting
  public void createLinkedOwner(PadPipelineCrossing padPipelineCrossing, List<Integer> owners) {
    var orgUnits = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(owners);
    orgUnits.forEach(portalOrganisationUnit -> {
      var owner = new PadPipelineCrossingOwner();
      owner.setPadPipelineCrossing(padPipelineCrossing);
      owner.setOrganisationUnit(portalOrganisationUnit);
      padPipelineCrossingOwnerRepository.save(owner);
    });
  }

  @VisibleForTesting
  public void createManualOwner(PadPipelineCrossing padPipelineCrossing, List<String> owners) {
    owners.forEach(s -> {
      var owner = new PadPipelineCrossingOwner();
      owner.setPadPipelineCrossing(padPipelineCrossing);
      owner.setManualOrganisationEntry(StringUtils.stripStart(s, SearchSelectable.FREE_TEXT_PREFIX));
      padPipelineCrossingOwnerRepository.save(owner);
    });
  }

}
