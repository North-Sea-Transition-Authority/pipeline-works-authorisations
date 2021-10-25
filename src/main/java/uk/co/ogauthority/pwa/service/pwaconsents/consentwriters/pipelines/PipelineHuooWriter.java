package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentPipelineOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;

@Service
public class PipelineHuooWriter implements ConsentWriter {

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PwaConsentPipelineOrganisationRoleService pwaConsentOrganisationPipelineRoleService;

  @Autowired
  public PipelineHuooWriter(PadPipelinesHuooService padPipelinesHuooService,
                            PwaConsentPipelineOrganisationRoleService pwaConsentOrganisationPipelineRoleService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pwaConsentOrganisationPipelineRoleService = pwaConsentOrganisationPipelineRoleService;
  }

  @Override
  public int getExecutionOrder() {
    return 20;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return applicationTaskSet.contains(ApplicationTask.PIPELINES_HUOO);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail,
                                PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    // get pad pipeline links and map to pipeline
    Map<Pipeline, List<PipelineOrganisationRoleLink>> pipelineToPadPipeRoleLinkMap = padPipelinesHuooService
        .getPadPipelineOrgRoleLinksForDetail(pwaApplicationDetail)
        .stream()
        .collect(Collectors.groupingBy(PipelineOrganisationRoleLink::getPipeline));

    // get consent pipeline links and map to pipeline
    Map<Pipeline, List<PipelineOrganisationRoleLink>> pipelineToConsentPipeRoleLinkMap = pwaConsentOrganisationPipelineRoleService
        .getActiveConsentedPipelineOrgRoleLinks(pwaConsent.getMasterPwa())
        .stream()
        .collect(Collectors.groupingBy(PipelineOrganisationRoleLink::getPipeline));

    // populate a list of consented links to end if they are not present on the app any more
    var consentPipeLinksToEnd = new ArrayList<PipelineOrganisationRoleLink>();
    addToListIfPipeLinksDontMatch(pipelineToConsentPipeRoleLinkMap, pipelineToPadPipeRoleLinkMap, consentPipeLinksToEnd);
    var linksToEndAsConsentLinks = consentPipeLinksToEnd.stream()
        .map(PwaConsentPipelineOrganisationRoleLink.class::cast)
        .collect(Collectors.toList());

    // populate a list of app links to create consented links for if they don't already exist in consented model
    var padPipeLinksToWriteToConsent = new ArrayList<PipelineOrganisationRoleLink>();
    addToListIfPipeLinksDontMatch(pipelineToPadPipeRoleLinkMap, pipelineToConsentPipeRoleLinkMap, padPipeLinksToWriteToConsent);
    var linksToCreateAsPadLinks = padPipeLinksToWriteToConsent.stream()
        .map(PadPipelineOrganisationRoleLink.class::cast)
        .collect(Collectors.toList());

    pwaConsentOrganisationPipelineRoleService.endRoleLinks(linksToEndAsConsentLinks, pwaConsent);

    var newConsentedLinks = pwaConsentOrganisationPipelineRoleService
        .createRoleLinks(linksToCreateAsPadLinks, consentWriterDto.getActiveConsentRoles(), pwaConsent);

    var extantPreviouslyConsentedLinks = pipelineToConsentPipeRoleLinkMap.values().stream()
        .flatMap(List::stream)
        .map(PwaConsentPipelineOrganisationRoleLink.class::cast)
        .filter(link -> !consentPipeLinksToEnd.contains(link))
        .collect(Collectors.toList());

    var activeLinks = new ArrayList<PwaConsentPipelineOrganisationRoleLink>();
    activeLinks.addAll(newConsentedLinks);
    activeLinks.addAll(extantPreviouslyConsentedLinks);

    endLinksForPipelinesNoLongerOnSeabed(consentWriterDto, activeLinks, pwaConsent);

    return consentWriterDto;

  }

  /**
   * Remove any consented (newly or pre-existing) links to pipelines no longer on the seabed.
   */
  private void endLinksForPipelinesNoLongerOnSeabed(ConsentWriterDto consentWriterDto,
                                                    List<PwaConsentPipelineOrganisationRoleLink> activeConsentLinks,
                                                    PwaConsent consent) {

    var onSeabedStatuses = PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED);
    var linksToEnd = new ArrayList<PwaConsentPipelineOrganisationRoleLink>();

    activeConsentLinks.forEach(link ->

        // if the pipeline was on the app and transitioned to a 'not on seabed' status, end the links
        Optional.ofNullable(consentWriterDto.getPipelineToNewDetailMap().get(link.getPipeline()))
            .map(PipelineDetail::getPipelineStatus)
            .ifPresent(pipelineStatus -> {

              boolean pipelineOnSeabed = onSeabedStatuses.contains(pipelineStatus);

              if (!pipelineOnSeabed) {
                linksToEnd.add(link);
              }

            })

    );

    pwaConsentOrganisationPipelineRoleService.endRoleLinks(linksToEnd, consent);

  }

  private void addToListIfPipeLinksDontMatch(Map<Pipeline, List<PipelineOrganisationRoleLink>> pipelineToRoleLinkMap,
                                             Map<Pipeline, List<PipelineOrganisationRoleLink>> pipelineToComparisonRoleLinkMap,
                                             List<PipelineOrganisationRoleLink> listToAddTo) {

    pipelineToRoleLinkMap.forEach((pipeline, roleLinks) -> {

      var comparisonRoleLinks = pipelineToComparisonRoleLinkMap.getOrDefault(pipeline, List.of());

      roleLinks.forEach(roleLink -> {

        boolean comparisonRoleLinksMatch = comparisonRoleLinks.stream()
            .anyMatch(comparisonLink -> linksMatch(roleLink, comparisonLink));

        if (!comparisonRoleLinksMatch) {
          listToAddTo.add(roleLink);
        }

      });

    });

  }

  private boolean linksMatch(PipelineOrganisationRoleLink firstLink,
                             PipelineOrganisationRoleLink comparisonLink) {

    boolean orgsMatch = false;
    var firstLinkOrgUnitIdOpt = firstLink.getOrgUnitId();
    var comparisonLinkOrgUnitIdOpt = comparisonLink.getOrgUnitId();

    if (firstLinkOrgUnitIdOpt.isPresent() && comparisonLinkOrgUnitIdOpt.isPresent()) {
      orgsMatch = firstLinkOrgUnitIdOpt.get().asInt() == comparisonLinkOrgUnitIdOpt.get().asInt();
    }

    boolean treatiesMatch = false;
    var firstLinkTreatyOpt = firstLink.getAgreement();
    var comparisonLinkTreatyOpt = comparisonLink.getAgreement();
    if (firstLinkTreatyOpt.isPresent() && comparisonLinkTreatyOpt.isPresent()) {
      treatiesMatch = firstLinkTreatyOpt.get() == comparisonLinkTreatyOpt.get();
    }

    boolean rolesAndSplitsMatch = linkRolesAndSplitsMatch(firstLink, comparisonLink);

    return (orgsMatch || treatiesMatch) && rolesAndSplitsMatch;

  }

  private boolean linkRolesAndSplitsMatch(PipelineOrganisationRoleLink firstLink,
                                          PipelineOrganisationRoleLink comparisonLink) {

    boolean rolesMatch = Objects.equals(firstLink.getRole(), comparisonLink.getRole());

    if (!rolesMatch) {
      return false;
    }

    // if roles match, check split information
    return Objects.equals(firstLink.getFromLocation(), comparisonLink.getFromLocation())
        && Objects.equals(firstLink.getFromLocationIdentInclusionMode(), comparisonLink.getFromLocationIdentInclusionMode())
        && Objects.equals(firstLink.getToLocation(), comparisonLink.getToLocation())
        && Objects.equals(firstLink.getToLocationIdentInclusionMode(), comparisonLink.getToLocationIdentInclusionMode())
        && Objects.equals(firstLink.getSectionNumber(), comparisonLink.getSectionNumber());

  }

}
