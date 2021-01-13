package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.model.search.SearchSelectionView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingOwnerRepository;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@Service
public class PadPipelineCrossingOwnerService {

  private final PadPipelineCrossingOwnerRepository padPipelineCrossingOwnerRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PadPipelineCrossingOwnerService(
      PadPipelineCrossingOwnerRepository padPipelineCrossingOwnerRepository,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      SearchSelectorService searchSelectorService) {
    this.padPipelineCrossingOwnerRepository = padPipelineCrossingOwnerRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.searchSelectorService = searchSelectorService;
  }

  public List<PadPipelineCrossingOwner> getOwnersForCrossing(PadPipelineCrossing padPipelineCrossing) {
    return padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing);
  }

  public Map<String, String> getOwnerPrepopulationFormAttribute(PadPipelineCrossing padPipelineCrossing) {
    var owners = getOwnersForCrossing(padPipelineCrossing);
    var result = new LinkedHashMap<String, String>();
    for (var owner : owners) {
      if (owner.isManualEntry()) {
        String manualId = SearchSelectable.FREE_TEXT_PREFIX + owner.getManualOrganisationEntry();
        result.put(manualId, owner.getManualOrganisationEntry());
      } else {
        String linkedId = String.valueOf(owner.getOrganisationUnit().getOuId());
        result.put(linkedId, owner.getOrganisationUnit().getName());
      }
    }
    return result;
  }

  public void createOwners(PadPipelineCrossing pipelineCrossing, PipelineCrossingForm form) {
    removeAllForCrossing(pipelineCrossing);
    if (!BooleanUtils.isTrue(form.getPipelineFullyOwnedByOrganisation())) {

      var selectionView = new SearchSelectionView<>(form.getPipelineOwners(), Integer::parseInt);

      createLinkedOwner(pipelineCrossing, selectionView.getLinkedEntries());
      createManualOwner(pipelineCrossing, selectionView.getManualEntries());
    }
  }

  private void createLinkedOwner(PadPipelineCrossing padPipelineCrossing, List<Integer> owners) {
    var orgUnits = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(owners);
    orgUnits.forEach(portalOrganisationUnit -> {
      var owner = new PadPipelineCrossingOwner();
      owner.setPadPipelineCrossing(padPipelineCrossing);
      owner.setOrganisationUnit(portalOrganisationUnit);
      padPipelineCrossingOwnerRepository.save(owner);
    });
  }

  private void createManualOwner(PadPipelineCrossing padPipelineCrossing, List<String> owners) {
    owners.forEach(s -> {
      var owner = new PadPipelineCrossingOwner();
      owner.setPadPipelineCrossing(padPipelineCrossing);
      owner.setManualOrganisationEntry(searchSelectorService.removePrefix(s));
      padPipelineCrossingOwnerRepository.save(owner);
    });
  }

  public void removeAllForCrossing(PadPipelineCrossing padPipelineCrossing) {
    var existingOwners = padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing);
    padPipelineCrossingOwnerRepository.deleteAll(existingOwners);
  }

  public List<PadPipelineCrossingOwner> getAllPipelineCrossingOwners(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing_PwaApplicationDetail(pwaApplicationDetail);
  }

}
