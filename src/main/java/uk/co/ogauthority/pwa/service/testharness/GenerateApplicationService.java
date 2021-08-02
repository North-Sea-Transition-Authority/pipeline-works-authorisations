package uk.co.ogauthority.pwa.service.testharness;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pickpwa.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;

@Service
@Profile("test-harness")
class GenerateApplicationService {

  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final TaskListService taskListService;
  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final Map<ApplicationTask, TestHarnessAppFormService> appTaskAndGeneratorServiceMap;

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateApplicationService.class);


  @Autowired
  public GenerateApplicationService(
      PwaApplicationCreationService pwaApplicationCreationService,
      TaskListService taskListService,
      PickedPwaRetrievalService pickedPwaRetrievalService,
      List<TestHarnessAppFormService> appFormServices) {
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.taskListService = taskListService;
    this.pickedPwaRetrievalService = pickedPwaRetrievalService;

    this.appTaskAndGeneratorServiceMap = appFormServices.stream()
        .collect(Collectors.toMap(TestHarnessAppFormService::getLinkedAppFormTask, Function.identity()));
  }


  PwaApplicationDetail generateInitialPwaApplication(Integer pipelineQuantity, WebUserAccount applicantUser) {
    var pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(applicantUser);
    setupAndRunAppTasks(pwaApplicationDetail, applicantUser, pipelineQuantity);
    return pwaApplicationDetail;
  }


  PwaApplicationDetail generateVariationPwaApplication(PwaApplicationType pwaApplicationType,
                                                       Integer consentedMasterPwaId,
                                                       Integer nonConsentedMasterPwaId,
                                                       Integer pipelineQuantity,
                                                       WebUserAccount applicantUser) {

    var pickedMasterPwaId = consentedMasterPwaId != null ? consentedMasterPwaId : nonConsentedMasterPwaId;
    var pickedPwa = pickedPwaRetrievalService.getPickedConsentedPwa(pickedMasterPwaId, applicantUser);

    var pwaApplicationDetail = pwaApplicationCreationService.createVariationPwaApplication(
        applicantUser,
        pickedPwa,
        pwaApplicationType);

    setupAndRunAppTasks(pwaApplicationDetail, applicantUser, pipelineQuantity);
    return pwaApplicationDetail;
  }

  private void setupAndRunAppTasks(PwaApplicationDetail pwaApplicationDetail,
                                   WebUserAccount user,
                                   Integer pipelineQuantity) {

    LOGGER.info("{} application detail created with id: {} and app ref: {}",
        pwaApplicationDetail.getPwaApplicationType().getDisplayName(),
        pwaApplicationDetail.getId(), pwaApplicationDetail.getPwaApplicationRef());

    var appFormServiceParams = new TestHarnessAppFormServiceParams(user, pwaApplicationDetail, pipelineQuantity);

    Set<ApplicationTask> preliminaryAppTasks = new HashSet<>(taskListService.getShownApplicationTasksForDetail(pwaApplicationDetail));
    generateAppTasks(preliminaryAppTasks, appFormServiceParams);

    //Need to do a second pass on getting the required tasks as certain tasks only become required after others are completed
    Set<ApplicationTask> updatedAppTasks = new HashSet<>(taskListService.getShownApplicationTasksForDetail(pwaApplicationDetail));
    var uncompletedAppTasks = SetUtils.difference(updatedAppTasks, preliminaryAppTasks);
    generateAppTasks(uncompletedAppTasks, appFormServiceParams);

    LOGGER.info("App form sections generated successfully for detail with id: {}", pwaApplicationDetail.getId());
  }

  private void generateAppTasks(Collection<ApplicationTask> applicationTasks, TestHarnessAppFormServiceParams appFormServiceParams) {
    applicationTasks.stream().sorted(Comparator.comparing(ApplicationTask::getDisplayOrder))
        .forEach(requiredTask -> {
          LOGGER.info("Generating app task {}", requiredTask.getDisplayName());
          appTaskAndGeneratorServiceMap.get(requiredTask).generateAppFormData(appFormServiceParams);
        });
  }








}
