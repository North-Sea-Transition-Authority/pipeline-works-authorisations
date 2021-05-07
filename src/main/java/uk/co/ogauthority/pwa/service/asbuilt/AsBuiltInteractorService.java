package uk.co.ogauthority.pwa.service.asbuilt;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Allows interaction with the as built notification domain.
 */
@Service
public class AsBuiltInteractorService {

  private final AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository;
  private final AsBuiltGroupStatusService asBuiltGroupStatusService;
  private final AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;


  private final Clock clock;

  public AsBuiltInteractorService(AsBuiltNotificationGroupRepository asBuiltNotificationGroupRepository,
                                  AsBuiltGroupStatusService asBuiltGroupStatusService,
                                  AsBuiltGroupDeadlineService asBuiltGroupDeadlineService,
                                  AsBuiltPipelineNotificationService asBuiltPipelineNotificationService,
                                  @Qualifier("utcClock") Clock clock) {
    this.asBuiltNotificationGroupRepository = asBuiltNotificationGroupRepository;
    this.asBuiltGroupStatusService = asBuiltGroupStatusService;
    this.asBuiltGroupDeadlineService = asBuiltGroupDeadlineService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
    this.clock = clock;
  }


  /**
   * Call this to create an as built notification.
   *
   * @param pwaConsent                - which consent is the as-built notification being created against.
   * @param reference                 - what should the as-built notification reference be?
   * @param deadlineDate              - what is the original deadline date going to be for this as built notification?
   * @param person                    - the person creating the as built notification. (System person when automatic or consent issuer person?).
   * @param pipelineNotificationSpecs A list of spec objects capturing which pipelines are included in the notification group.
   */
  @Transactional
  public void createAsBuiltNotification(PwaConsent pwaConsent,
                                        String reference,
                                        LocalDate deadlineDate,
                                        Person person,
                                        List<AsBuiltPipelineNotificationSpec> pipelineNotificationSpecs) {
    var asBuiltGroup = createAsBuiltNotificationGroup(pwaConsent, reference);

    asBuiltGroupStatusService.setNewTipStatus(asBuiltGroup, AsBuiltNotificationGroupStatus.NOT_STARTED, person);
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

  public LocalDate createDefaultAsBuiltGroupDeadlineDate(Instant consentIssuedDateTime,
                                                         PwaApplicationType consentApplicationType,
                                                         Instant completionDateOnApplication) {

    // By default the business rules outlining the default deadline for as built notifications are
    // options application consents, this is 1 week (7 calendar days) after consent.
    // for all other applications types, 1 week after completion date on application
    if (consentApplicationType.equals(PwaApplicationType.OPTIONS_VARIATION)) {
      return LocalDate.ofInstant(
          consentIssuedDateTime.truncatedTo(ChronoUnit.DAYS).plus(7, ChronoUnit.DAYS),
          ZoneId.systemDefault()
      );
    }

    return LocalDate.ofInstant(
        completionDateOnApplication.truncatedTo(ChronoUnit.DAYS).plus(7, ChronoUnit.DAYS),
        ZoneId.systemDefault()
    );
  }

}
