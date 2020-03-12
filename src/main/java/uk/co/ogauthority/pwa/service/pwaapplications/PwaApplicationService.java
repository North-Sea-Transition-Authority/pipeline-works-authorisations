package uk.co.ogauthority.pwa.service.pwaapplications;

import java.time.Clock;
import java.time.Instant;
import javax.transaction.Transactional;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to manage and manipulate PWAs and their applications.
 */
@Service
public class PwaApplicationService {


  private final MasterPwaManagementService masterPwaManagementService;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;

  @Autowired
  public PwaApplicationService(MasterPwaManagementService masterPwaManagementService,
                               PwaApplicationRepository pwaApplicationRepository,
                               PwaApplicationDetailRepository pwaApplicationDetailRepository,
                               CamundaWorkflowService camundaWorkflowService,
                               @Qualifier("utcClock") Clock clock) {
    this.masterPwaManagementService = masterPwaManagementService;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
  }

  @Transactional
  public PwaApplication createInitialPwaApplication(WebUserAccount createdByUser) {

    var creationInstant = Instant.now(clock);
    MasterPwaDetail masterPwaDetail = masterPwaManagementService.createMasterPwa(
        MasterPwaDetailStatus.APPLICATION,
        "New Pwa " + RandomUtils.nextInt()
    );

    var application = new PwaApplication(masterPwaDetail.getMasterPwa(), PwaApplicationType.INITIAL, 0);
    pwaApplicationRepository.save(application);

    var detail = new PwaApplicationDetail(application, 1, createdByUser.getWuaId(), creationInstant);
    pwaApplicationDetailRepository.save(detail);

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, application.getId());

    return application;

  }

  @Transactional
  public PwaApplication createVariationPwaApplication(WebUserAccount createdByUser, MasterPwa masterPwa,
                                                      PwaApplicationType pwaApplicationType) {

    var creationInstant = Instant.now(clock);

    var application = new PwaApplication(masterPwa, pwaApplicationType, 0);
    pwaApplicationRepository.save(application);

    var detail = new PwaApplicationDetail(application, 1, createdByUser.getWuaId(), creationInstant);
    pwaApplicationDetailRepository.save(detail);

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, application.getId());

    return application;

  }

  public PwaApplication getApplicationFromId(int applicationId) {
    return pwaApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find application with id " + applicationId));
  }

}
