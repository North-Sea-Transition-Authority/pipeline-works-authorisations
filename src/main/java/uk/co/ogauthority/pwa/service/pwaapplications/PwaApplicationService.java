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
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaRepository;
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
  private final MasterPwaDetailRepository masterPwaDetailRepository;
  private final PwaApplicationRepository pwaApplicationRepository;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;

  @Autowired
  public PwaApplicationService(MasterPwaRepository masterPwaRepository,
                               MasterPwaDetailRepository masterPwaDetailRepository,
                               PwaApplicationRepository pwaApplicationRepository,
                               PwaApplicationDetailRepository pwaApplicationDetailRepository,
                               CamundaWorkflowService camundaWorkflowService,
                               @Qualifier("utcClock") Clock clock) {
    this.masterPwaRepository = masterPwaRepository;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
    this.pwaApplicationRepository = pwaApplicationRepository;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
  }

  @Transactional
  public PwaApplication createInitialPwaApplication(WebUserAccount createdByUser) {

    var creationInstant = Instant.now(clock);

    var masterPwa = new MasterPwa(creationInstant);
    masterPwaRepository.save(masterPwa);

    var masterPwaDetail = new MasterPwaDetail(creationInstant);
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.APPLICATION);
    masterPwaDetail.setReference("New Pwa " + RandomUtils.nextInt());
    masterPwaDetailRepository.save(masterPwaDetail);

    var application = new PwaApplication(masterPwa, PwaApplicationType.INITIAL, 0);
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
