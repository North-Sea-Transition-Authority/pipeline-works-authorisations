package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;

/**
 * Supports the top level HUOO task based processing requirements.
 */
@Service
public class PadHuooTaskSectionService implements ApplicationFormSectionService {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;
  private final PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;
  private final PadOptionConfirmedService padOptionConfirmedService;
  private final PadHuooValidationService padHuooValidationService;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadHuooTaskSectionService(
      PadOrganisationRolesRepository padOrganisationRolesRepository,
      PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository,
      PadOptionConfirmedService padOptionConfirmedService,
      PadHuooValidationService padHuooValidationService,
      EntityCopyingService entityCopyingService) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
    this.padPipelineOrganisationRoleLinkRepository = padPipelineOrganisationRoleLinkRepository;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.padHuooValidationService = padHuooValidationService;
    this.entityCopyingService = entityCopyingService;
  }


  @Override
  public boolean allowCopyOfSectionInformation(PwaApplicationDetail pwaApplicationDetail) {
    // Always copy huoo information when Options
    return PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())
        || canShowInTaskList(pwaApplicationDetail);
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    var validTypes = EnumSet.complementOf(EnumSet.of(PwaApplicationType.DEPOSIT_CONSENT, PwaApplicationType.OPTIONS_VARIATION));

    return validTypes.contains(pwaApplicationDetail.getPwaApplicationType())
        || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    var validationResult = padHuooValidationService.getHuooSummaryValidationResult(detail);
    return validationResult.isValid();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    throw new UnsupportedOperationException("This validate method should not be used. Use PadHuooValidationService instead.");

  }


  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    var padOrgRoleCopiedEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padOrganisationRolesRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadOrganisationRole.class
    );

    var pipelineOrgRolesCopiedEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padPipelineOrganisationRoleLinkRepository.getAllByPadOrgRole_PwaApplicationDetail(fromDetail),
        padOrgRoleCopiedEntityIds,
        PadPipelineOrganisationRoleLink.class
    );

  }
}
