package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import javax.transaction.Transactional;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to perform business logic for PWA application creation.
 */
@Service
public class PwaApplicationCreationService {

  private final MasterPwaManagementService masterPwaManagementService;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaContactService pwaContactService;
  private final Clock clock;

  @Autowired
  public PwaApplicationCreationService(MasterPwaManagementService masterPwaManagementService,
                                       PwaApplicationRepository pwaApplicationRepository,
                                       PwaApplicationDetailRepository pwaApplicationDetailRepository,
                                       CamundaWorkflowService camundaWorkflowService,
                                       PwaContactService pwaContactService,
                                       @Qualifier("utcClock") Clock clock) {
    this.masterPwaManagementService = masterPwaManagementService;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaContactService = pwaContactService;
    this.clock = clock;
  }

  private PwaApplication createApplication(MasterPwa masterPwa,
                                           PwaApplicationType applicationType,
                                           int variationNo,
                                           WebUserAccount createdByUser) {

    var application = new PwaApplication(masterPwa, applicationType, variationNo);
    application.setAppReference("APP/" + RandomUtils.nextInt());
    pwaApplicationRepository.save(application);

    pwaContactService.addContact(
        application,
        createdByUser.getLinkedPerson(),
        Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.SUBMITTER));

    var detail = new PwaApplicationDetail(application, 1, createdByUser.getWuaId(), Instant.now(clock));
    pwaApplicationDetailRepository.save(detail);

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, application.getId());

    return application;

  }

  @Transactional
  public PwaApplication createInitialPwaApplication(WebUserAccount createdByUser) {

    MasterPwaDetail masterPwaDetail = masterPwaManagementService.createMasterPwa(
        MasterPwaDetailStatus.APPLICATION,
        "New Pwa " + RandomUtils.nextInt()
    );

    var masterPwa = masterPwaDetail.getMasterPwa();

    return createApplication(masterPwa, PwaApplicationType.INITIAL, 0, createdByUser);

  }

  @Transactional
  public PwaApplication createVariationPwaApplication(WebUserAccount createdByUser, MasterPwa masterPwa,
                                                      PwaApplicationType pwaApplicationType) {

    return createApplication(masterPwa, pwaApplicationType, 0, createdByUser);

  }


}
