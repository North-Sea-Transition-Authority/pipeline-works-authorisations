package uk.co.ogauthority.pwa.features.application.creation;

import java.time.Clock;
import java.util.EnumSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

/**
 * Service to perform business logic for PWA application creation.
 */
@Service
public class PwaApplicationCreationService {

  private final MasterPwaService masterPwaService;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationReferencingService pwaApplicationReferencingService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final MasterPwaDetailFieldService masterPwaDetailFieldService;
  private final PadFieldService padFieldService;
  private final Clock clock;


  @Autowired
  public PwaApplicationCreationService(MasterPwaService masterPwaService,
                                       PwaApplicationRepository pwaApplicationRepository,
                                       CamundaWorkflowService camundaWorkflowService,
                                       PwaContactService pwaContactService,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       PwaApplicationReferencingService pwaApplicationReferencingService,
                                       PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                       PadOrganisationRoleService padOrganisationRoleService,
                                       MasterPwaDetailFieldService masterPwaDetailFieldService,
                                       PadFieldService padFieldService,
                                       @Qualifier("utcClock") Clock clock) {
    this.masterPwaService = masterPwaService;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationReferencingService = pwaApplicationReferencingService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.masterPwaDetailFieldService = masterPwaDetailFieldService;
    this.padFieldService = padFieldService;
    this.clock = clock;
  }

  private PwaApplicationDetail createApplication(MasterPwa masterPwa,
                                                 PwaApplicationType applicationType,
                                                 int variationNo,
                                                 WebUserAccount createdByUser,
                                                 PortalOrganisationUnit applicantOrganisationUnit) {

    var application = new PwaApplication(masterPwa, applicationType, variationNo);
    application.setAppReference(pwaApplicationReferencingService.createAppReference());
    application.setApplicationCreatedTimestamp(clock.instant());
    application.setApplicantOrganisationUnitId(OrganisationUnitId.from(applicantOrganisationUnit));
    pwaApplicationRepository.save(application);

    pwaContactService.updateContact(
        application,
        createdByUser.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));

    var activeHoldersCount = application.getApplicationType().equals(PwaApplicationType.INITIAL) ? 1
        : pwaConsentOrganisationRoleService.getNumberOfHolders(masterPwa);

    var detail = pwaApplicationDetailService.createFirstDetail(application, createdByUser, activeHoldersCount);

    camundaWorkflowService.startWorkflow(application);

    var createHuooDataForAppTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.HUOO_VARIATION,
        PwaApplicationType.OPTIONS_VARIATION
    );

    // Its possible this can be done more cleverly if some simple link between app type and app task existed.
    // Possible only with large effort to link appTask to business logic code, current link sits at controller annotation level.
    if (createHuooDataForAppTypes.contains(applicationType)) {
      var consentedHuooSummary = pwaConsentOrganisationRoleService.getActiveOrganisationRoleSummaryForSeabedPipelines(masterPwa);
      padOrganisationRoleService.createApplicationOrganisationRolesFromSummary(detail, consentedHuooSummary);
    }

    return detail;

  }

  @Transactional
  public PwaApplicationDetail createInitialPwaApplication(PortalOrganisationUnit applicantOrganisationUnit,
                                                          WebUserAccount createdByUser) {

    MasterPwaDetail masterPwaDetail = masterPwaService.createMasterPwa(
        MasterPwaDetailStatus.APPLICATION,
        // this will be updated immediately when the app reference is available
        "New Pwa " + RandomUtils.nextInt()
    );

    var masterPwa = masterPwaDetail.getMasterPwa();

    var newApplication = createApplication(masterPwa, PwaApplicationType.INITIAL, 0, createdByUser, applicantOrganisationUnit);
    masterPwaService.updateDetailReference(masterPwaDetail, newApplication.getPwaApplicationRef());

    return newApplication;
  }

  @Transactional
  public PwaApplicationDetail createVariationPwaApplication(MasterPwa masterPwa,
                                                            PwaApplicationType pwaApplicationType,
                                                            PortalOrganisationUnit applicantOrganisationUnit,
                                                            WebUserAccount createdByUser) {

    var applicationDetail = createApplication(masterPwa, pwaApplicationType, 0, createdByUser, applicantOrganisationUnit);

    var masterPwaDetailFields = masterPwaDetailFieldService.getMasterPwaDetailFields(masterPwa);

    padFieldService.createAndSavePadFieldsFromMasterPwa(applicationDetail,
        masterPwaService.getCurrentDetailOrThrow(masterPwa), masterPwaDetailFields);

    return applicationDetail;

  }

}
