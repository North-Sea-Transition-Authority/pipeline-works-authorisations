package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.EnumSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to perform business logic for PWA application creation.
 */
@Service
public class PwaApplicationCreationService {

  private final MasterPwaManagementService masterPwaManagementService;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationReferencingService pwaApplicationReferencingService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PadOrganisationRoleService padOrganisationRoleService;


  @Autowired
  public PwaApplicationCreationService(MasterPwaManagementService masterPwaManagementService,
                                       PwaApplicationRepository pwaApplicationRepository,
                                       CamundaWorkflowService camundaWorkflowService,
                                       PwaContactService pwaContactService,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       PwaApplicationReferencingService pwaApplicationReferencingService,
                                       PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                       PadOrganisationRoleService padOrganisationRoleService) {
    this.masterPwaManagementService = masterPwaManagementService;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationReferencingService = pwaApplicationReferencingService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  private PwaApplicationDetail createApplication(MasterPwa masterPwa,
                                                 PwaApplicationType applicationType,
                                                 int variationNo,
                                                 WebUserAccount createdByUser) {

    var application = new PwaApplication(masterPwa, applicationType, variationNo);
    application.setAppReference(pwaApplicationReferencingService.createAppReference());
    pwaApplicationRepository.save(application);

    pwaContactService.addContact(
        application,
        createdByUser.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER));

    var detail = pwaApplicationDetailService.createFirstDetail(application, createdByUser);

    camundaWorkflowService.startWorkflow(application);

    var createHuooDataForAppTypes = EnumSet.of(
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.HUOO_VARIATION
    );

    // Its possible this can be done more cleverly if some simple link between app type and app task existed.
    // Possible only with large effort to link appTask to business logic code, current link sits at controller annotation level.
    if (createHuooDataForAppTypes.contains(applicationType)) {
      var consentedHuooSummary = pwaConsentOrganisationRoleService.getOrganisationRoleSummary(masterPwa);
      padOrganisationRoleService.createApplicationOrganisationRolesFromSummary(detail, consentedHuooSummary);
    }

    var activeHoldersCount = pwaConsentOrganisationRoleService.getNumberOfHolders(masterPwa);
    detail.setNumOfHolders(Math.toIntExact(activeHoldersCount));

    return detail;

  }

  @Transactional
  public PwaApplicationDetail createInitialPwaApplication(WebUserAccount createdByUser) {

    MasterPwaDetail masterPwaDetail = masterPwaManagementService.createMasterPwa(
        MasterPwaDetailStatus.APPLICATION,
        // TODO PWA-480 implement referencing
        "New Pwa " + RandomUtils.nextInt()
    );

    var masterPwa = masterPwaDetail.getMasterPwa();

    return createApplication(masterPwa, PwaApplicationType.INITIAL, 0, createdByUser);

  }

  @Transactional
  public PwaApplicationDetail createVariationPwaApplication(WebUserAccount createdByUser,
                                                            MasterPwa masterPwa,
                                                            PwaApplicationType pwaApplicationType) {

    var applicationDetail = createApplication(masterPwa, pwaApplicationType, 0, createdByUser);

    return applicationDetail;

  }

}
