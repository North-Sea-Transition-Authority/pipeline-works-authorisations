package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

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

  @Autowired
  public PwaApplicationCreationService(MasterPwaManagementService masterPwaManagementService,
                                       PwaApplicationRepository pwaApplicationRepository,
                                       CamundaWorkflowService camundaWorkflowService,
                                       PwaContactService pwaContactService,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       PwaApplicationReferencingService pwaApplicationReferencingService) {
    this.masterPwaManagementService = masterPwaManagementService;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationReferencingService = pwaApplicationReferencingService;
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

    return createApplication(masterPwa, pwaApplicationType, 0, createdByUser);

  }

}
