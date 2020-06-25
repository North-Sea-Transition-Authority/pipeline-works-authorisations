package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelinesFormValidator;

@Service
public class PadPipelinesHuooService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelinesHuooService.class);

  private final PickablePipelineService pickablePipelineService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator;
  private final PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;


  @Autowired
  public PadPipelinesHuooService(
      PickablePipelineService pickablePipelineService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadOrganisationRoleService padOrganisationRoleService,
      PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator,
      PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository) {
    this.pickablePipelineService = pickablePipelineService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pickHuooPipelinesFormValidator = pickHuooPipelinesFormValidator;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
  }


  public void validateAddPipelineHuooForm(PwaApplicationDetail pwaApplicationDetail,
                                          PickHuooPipelinesForm form,
                                          BindingResult bindingResult,
                                          PickHuooPipelineValidationType pickHuooPipelineValidationType,
                                          HuooRole huooRole) {

    pickHuooPipelinesFormValidator.validate(form,
        bindingResult, List.of(
            huooRole,
            pwaApplicationDetail,
            pickHuooPipelineValidationType
        ).toArray());

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    LOGGER.info("is complete not implemented");
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    LOGGER.info("validate not implemented");
    return bindingResult;
  }


  /* Convenience method passing args to the actual pad org roles service */
  public List<PadOrganisationRole> getPadOrganisationRolesFrom(PwaApplicationDetail pwaApplicationDetail,
                                                               HuooRole huooRole,
                                                               Set<Integer> organisationUnitIds) {

    var orgUnitIds = organisationUnitIds.stream()
        .map(OrganisationUnitId::new)
        .collect(toSet());

    return padOrganisationRoleService.getOrgRolesForDetailByOrganisationIdAndRole(
        pwaApplicationDetail,
        orgUnitIds,
        huooRole);

  }

  @Transactional
  public void createPipelineOrganisationRoles(PwaApplicationDetail pwaApplicationDetail,
                                              List<PadOrganisationRole> padOrganisationRoles,
                                              Set<Pipeline> pipelines) {

    // This is probably ok...batch inserts aren't really possible atm due to using IDENTITY primary key columns.
    for (PadOrganisationRole padOrganisationRole : padOrganisationRoles) {
      for (Pipeline pipeline : pipelines) {
        padOrganisationRoleService.createPadPipelineOrganisationRoleLink(padOrganisationRole, pipeline);
      }
    }

  }

  public List<PickablePipelineOption> getPickablePipelineOptionsWithNoRoleOfType(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole) {

    Set<PipelineId> pipelineIdsWithRole = padOrganisationRoleService.getPipelineIdsWhereRoleOfTypeSet(
        pwaApplicationDetail, huooRole);

    Map<PickablePipelineId, PickablePipelineOption> allPickablePipelineOptions = pickablePipelineService
        .getAllPickablePipelinesForApplication(pwaApplicationDetail)
        .stream()
        .collect(toMap(PickablePipelineId::from, ppo -> ppo));

    // We want everything in the set of all pickable pipelines where the reconciled PipelineId
    // does not exist in the set of pipelineIds with an organisation role of the desired type.
    Set<PickablePipelineId> pickablePipelinesWithoutRole = pickablePipelineService.reconcilePickablePipelineOptions(
        new HashSet<>(allPickablePipelineOptions.values())
    )
        .stream()
        .filter(rpp -> !pipelineIdsWithRole.contains(rpp.getPipelineId()))
        .map(ReconciledPickablePipeline::getPickablePipelineId)
        .collect(toSet());

    return allPickablePipelineOptions.entrySet().stream()
        .filter(entry -> pickablePipelinesWithoutRole.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .sorted(Comparator.comparing(PickablePipelineOption::getPipelineNumber))
        .collect(Collectors.toList());

  }

  public List<OrganisationUnitDetailDto> getAvailableOrgUnitDetailsForRole(PwaApplicationDetail pwaApplicationDetail,
                                                                           HuooRole huooRole) {
    var orgUnitsForRole = padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail)
        .stream()
        .filter(o -> huooRole.equals(o.getRole()))
        .map(PadOrganisationRole::getOrganisationUnit)
        .collect(toList());

    return portalOrganisationsAccessor.getOrganisationUnitDetailDtos(orgUnitsForRole);

  }

  public PipelineAndOrganisationRoleGroupSummaryDto createPipelineAndOrganisationRoleGroupSummary(
      PwaApplicationDetail pwaApplicationDetail) {

    var allPipelineRolesForApp = padPipelineOrganisationRoleLinkRepository.findOrganisationPipelineRoleDtoByPwaApplicationDetail(
        pwaApplicationDetail);
    return PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(allPipelineRolesForApp);
  }


}
