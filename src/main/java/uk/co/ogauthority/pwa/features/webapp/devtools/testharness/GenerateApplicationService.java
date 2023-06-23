package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
public class GenerateApplicationService {

  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final TaskListService taskListService;
  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final Map<ApplicationTask, TestHarnessAppFormService> appTaskAndGeneratorServiceMap;
  private final TestHarnessOrganisationUnitService testHarnessOrganisationUnitService;

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateApplicationService.class);


  @Autowired
  public GenerateApplicationService(
      PwaApplicationCreationService pwaApplicationCreationService,
      TaskListService taskListService,
      PickedPwaRetrievalService pickedPwaRetrievalService,
      List<TestHarnessAppFormService> appFormServices,
      TestHarnessOrganisationUnitService testHarnessOrganisationUnitService) {
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.taskListService = taskListService;
    this.pickedPwaRetrievalService = pickedPwaRetrievalService;

    this.appTaskAndGeneratorServiceMap = appFormServices.stream()
        .collect(Collectors.toMap(TestHarnessAppFormService::getLinkedAppFormTask, Function.identity()));
    this.testHarnessOrganisationUnitService = testHarnessOrganisationUnitService;
  }


  PwaApplicationDetail generateInitialPwaApplication(Integer pipelineQuantity, WebUserAccount applicantUser, PwaResourceType resourceType) {
    var applicantOrgUnit = testHarnessOrganisationUnitService
        .getFirstOrgUnitUserCanAccessOrThrow(applicantUser);
    var pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(
        applicantOrgUnit,
        applicantUser,
        resourceType
    );
    setupAndRunAppTasks(pwaApplicationDetail, applicantUser, pipelineQuantity);
    return pwaApplicationDetail;
  }


  PwaApplicationDetail generateVariationPwaApplication(PwaApplicationType pwaApplicationType,
                                                       Integer consentedMasterPwaId,
                                                       Integer nonConsentedMasterPwaId,
                                                       Integer pipelineQuantity,
                                                       WebUserAccount applicantUser,
                                                       PwaResourceType resourceType) {

    MasterPwa pickedPwa;
    if (consentedMasterPwaId != null) {
      pickedPwa = pickedPwaRetrievalService.getPickedConsentedPwa(consentedMasterPwaId, applicantUser);
    } else {
      pickedPwa = pickedPwaRetrievalService.getPickedNonConsentedPwa(nonConsentedMasterPwaId, applicantUser);
    }

    var applicantOrgUnit = testHarnessOrganisationUnitService
        .getFirstOrgUnitUserCanAccessOrThrow(applicantUser);

    var pwaApplicationDetail = pwaApplicationCreationService.createVariationPwaApplication(
        pickedPwa,
        pwaApplicationType,
        resourceType,
        applicantOrgUnit,
        applicantUser);

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

  public void generateAppTasks(Collection<ApplicationTask> applicationTasks, TestHarnessAppFormServiceParams appFormServiceParams) {
    applicationTasks.stream().sorted(Comparator.comparing(ApplicationTask::getDisplayOrder))
        .forEach(requiredTask -> {
          LOGGER.info("Generating app task {}", requiredTask.getDisplayName());
          appTaskAndGeneratorServiceMap.get(requiredTask).generateAppFormData(appFormServiceParams);
        });
  }

}
