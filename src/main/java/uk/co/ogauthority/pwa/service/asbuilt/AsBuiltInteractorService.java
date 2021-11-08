package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltSubmissionResult;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.model.form.asbuilt.ChangeAsBuiltNotificationGroupDeadlineForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * Allows interaction with the as built notification domain.
 */
@Service
public class AsBuiltInteractorService {

  private final AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;
  private final AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;
  private final AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;
  private final AsBuiltNotificationSubmissionService asBuiltNotificationSubmissionService;


  private final Clock clock;

  public AsBuiltInteractorService(AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository,
                                  AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService,
                                  AsBuiltGroupDeadlineService asBuiltGroupDeadlineService,
                                  AsBuiltPipelineNotificationService asBuiltPipelineNotificationService,
                                  AsBuiltNotificationSubmissionService asBuiltNotificationSubmissionService,
                                  @Qualifier("utcClock") Clock clock) {
    this.asBuiltNotificationGroupRepository = asBuiltNotificationGroupRepository;
    this.asBuiltNotificationGroupStatusService = asBuiltNotificationGroupStatusService;
    this.asBuiltGroupDeadlineService = asBuiltGroupDeadlineService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
    this.asBuiltNotificationSubmissionService = asBuiltNotificationSubmissionService;
    this.clock = clock;
  }


  /**
   * Call this to create an as built notification.
   *
   * @param pwaConsent   - which consent is the as-built notification being created against.
   * @param reference    - what should the as-built notification reference be?
   * @param deadlineDate - what is the original deadline date going to be for this as built notification?
   * @param person       - the person creating the as built notification. (System person when automatic or consent issuer person?).
   * @param pipelineNotificationSpecs A list of spec objects capturing which pipelines are included in the notification group.
   */
  @Transactional
  public void createAsBuiltNotification(PwaConsent pwaConsent,
                                        String reference,
                                        LocalDate deadlineDate,
                                        Person person,
                                        List<AsBuiltPipelineNotificationSpec> pipelineNotificationSpecs) {
    var asBuiltGroup = createAsBuiltNotificationGroup(pwaConsent, reference);

    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltGroup, AsBuiltNotificationGroupStatus.NOT_STARTED, person);
    asBuiltGroupDeadlineService.setNewDeadline(asBuiltGroup, deadlineDate, person);
    asBuiltPipelineNotificationService.addPipelineDetailsToAsBuiltNotificationGroup(asBuiltGroup,
        pipelineNotificationSpecs);

  }

  private AsBuiltNotificationGroup createAsBuiltNotificationGroup(PwaConsent pwaConsent,
                                                                  String reference) {

    var instant = clock.instant();
    var group = new AsBuiltNotificationGroup(pwaConsent, reference, instant);

    return asBuiltNotificationGroupRepository.save(group);
  }

  @Transactional
  public AsBuiltSubmissionResult submitAsBuiltNotification(AsBuiltNotificationGroupPipeline abngPipeline,
                                                           AsBuiltNotificationSubmissionForm form,
                                                           AuthenticatedUserAccount user) {
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(abngPipeline, form, user);
    return asBuiltNotificationGroupStatusService.isGroupStatusComplete(abngPipeline.getAsBuiltNotificationGroup())
        ? AsBuiltSubmissionResult.AS_BUILT_GROUP_COMPLETED
        : AsBuiltSubmissionResult.AS_BUILT_GROUP_IN_PROGRESS;
  }

  @Transactional
  public void setNewDeadlineDateForGroup(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                         ChangeAsBuiltNotificationGroupDeadlineForm form,
                                         AuthenticatedUserAccount user) {
    var deadlineDate = DateUtils.datePickerStringToDate(form.getNewDeadlineDateTimestampStr());
    asBuiltGroupDeadlineService.setNewDeadline(asBuiltNotificationGroup, deadlineDate, user.getLinkedPerson());
  }

  public void notifyHoldersOfAsBuiltGroupDeadlines() {
    asBuiltGroupDeadlineService.notifyHoldersOfAsBuiltGroupDeadlines();
  }

  @Transactional
  public void reopenAsBuiltNotificationGroup(AsBuiltNotificationGroup asBuiltNotificationGroup, Person person) {
    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS,
        person);
  }

}
