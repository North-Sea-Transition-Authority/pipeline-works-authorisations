package uk.co.ogauthority.pwa.service.pwaapplications;

import java.time.Clock;
import java.time.Instant;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.MasterPwaRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

/**
 * Service to manage and manipulate PWAs and their applications.
 */
@Service
public class PwaApplicationService {

  private final MasterPwaRepository masterPwaRepository;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;

  @Autowired
  public PwaApplicationService(MasterPwaRepository masterPwaRepository,
                               PwaApplicationRepository pwaApplicationRepository,
                               PwaApplicationDetailRepository pwaApplicationDetailRepository,
                               CamundaWorkflowService camundaWorkflowService,
                               @Qualifier("utcClock") Clock clock) {
    this.masterPwaRepository = masterPwaRepository;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
  }

  @Transactional
  public void createInitialPwaApplication(WebUserAccount createdByUser) {

    var creationInstant = Instant.now(clock);

    var masterPwa = new MasterPwa(creationInstant);
    masterPwaRepository.save(masterPwa);

    var application = new PwaApplication(masterPwa, PwaApplicationType.INITIAL, 0);
    pwaApplicationRepository.save(application);

    var detail = new PwaApplicationDetail(application, 1, createdByUser.getWuaId(), creationInstant);
    pwaApplicationDetailRepository.save(detail);

    camundaWorkflowService.startWorkflow(WorkflowType.PWA_APPLICATION, application.getId());

  }

}
