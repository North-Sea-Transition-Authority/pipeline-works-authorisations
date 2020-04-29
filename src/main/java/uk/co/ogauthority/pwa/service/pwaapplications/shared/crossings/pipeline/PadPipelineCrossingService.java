package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineCrossingService implements ApplicationFormSectionService {

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final PipelineCrossingFileService pipelineCrossingFileService;
  private final PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Autowired
  public PadPipelineCrossingService(
      PadPipelineCrossingRepository padPipelineCrossingRepository,
      PipelineCrossingFileService pipelineCrossingFileService,
      PadPipelineCrossingOwnerService padPipelineCrossingOwnerService,
      PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.pipelineCrossingFileService = pipelineCrossingFileService;
    this.padPipelineCrossingOwnerService = padPipelineCrossingOwnerService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  public PadPipelineCrossing getPipelineCrossing(PwaApplicationDetail detail, Integer id) {
    return padPipelineCrossingRepository.getByPwaApplicationDetailAndId(detail, id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find pipeline crossing with ID: " + id));
  }

  public void deleteCascade(PadPipelineCrossing padPipelineCrossing) {
    padPipelineCrossingOwnerService.removeAllForCrossing(padPipelineCrossing);
    padPipelineCrossingRepository.delete(padPipelineCrossing);
  }

  public List<PipelineCrossingView> getPipelineCrossingViews(PwaApplicationDetail pwaApplicationDetail) {
    var crossings = padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    return crossings.stream()
        .map(padPipelineCrossing -> {
          var owners = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
          return new PipelineCrossingView(padPipelineCrossing, owners);
        })
        .collect(Collectors.toList());
  }

  public PipelineCrossingView getPipelineCrossingView(PadPipelineCrossing padPipelineCrossing) {
    var owners = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    return new PipelineCrossingView(padPipelineCrossing, owners);
  }

  public void createPipelineCrossings(PwaApplicationDetail pwaApplicationDetail, PipelineCrossingForm form) {
    var crossing = new PadPipelineCrossing();
    crossing.setPwaApplicationDetail(pwaApplicationDetail);
    crossing.setPipelineCrossed(form.getPipelineCrossed());
    crossing.setPipelineFullyOwnedByOrganisation(form.getPipelineFullyOwnedByOrganisation());
    padPipelineCrossingRepository.save(crossing);
    padPipelineCrossingOwnerService.createOwners(crossing, form);
  }

  public void updatePipelineCrossing(PadPipelineCrossing crossing, PipelineCrossingForm form) {
    crossing.setPipelineCrossed(form.getPipelineCrossed());
    crossing.setPipelineFullyOwnedByOrganisation(form.getPipelineFullyOwnedByOrganisation());
    padPipelineCrossingRepository.save(crossing);
    padPipelineCrossingOwnerService.createOwners(crossing, form);

  }

  public Map<String, String> getPreselectedItems(List<String> selection) {
    var selectedItems = ListUtils.emptyIfNull(selection);
    var orgIds = selectedItems.stream()
        .filter(s -> !s.startsWith(SearchSelectable.FREE_TEXT_PREFIX))
        .map(Integer::parseInt)
        .collect(Collectors.toList());
    var orgs = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(orgIds);
    return selectedItems.stream()
        .collect(StreamUtils.toLinkedHashMap(s -> s, s -> {
          if (s.startsWith(SearchSelectable.FREE_TEXT_PREFIX)) {
            return StringUtils.stripStart(s, SearchSelectable.FREE_TEXT_PREFIX);
          } else {
            return orgs.stream()
                .filter(portalOrganisationUnit -> String.valueOf(portalOrganisationUnit.getOuId()).equals(s))
                .findAny()
                .orElseThrow(() -> new PwaEntityNotFoundException("Failed to find portal org unit with ID of: " + s))
                .getName();
          }
        }));
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return padPipelineCrossingRepository.countAllByPwaApplicationDetail(detail) > 0
        && pipelineCrossingFileService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }
}
