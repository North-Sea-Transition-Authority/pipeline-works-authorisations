package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationSpec;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * Do the logic to determine if an As-built notification is required, and which pipelines should be contained within it after a
 * consent has been issued.
 */
@Service
public class ConsentAsBuiltWriterService implements ConsentWriter {

  private final PipelineDetailService pipelineDetailService;
  private final AsBuiltInteractorService asBuiltInteractorService;
  private final PadProjectInformationService padProjectInformationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final UserAccountService userAccountService;

  private static final Set<PipelineStatus> OUT_OF_USE_OR_RTS_STATUSES = Set.of(
      PipelineStatus.OUT_OF_USE_ON_SEABED, PipelineStatus.RETURNED_TO_SHORE);

  private static final Set<PipelineStatus> NEW_PIPELINE_STATUSES = Set.of(
      PipelineStatus.IN_SERVICE, PipelineStatus.OUT_OF_USE_ON_SEABED);

  @Autowired
  public ConsentAsBuiltWriterService(PipelineDetailService pipelineDetailService,
                                     AsBuiltInteractorService asBuiltInteractorService,
                                     PadProjectInformationService padProjectInformationService,
                                     PwaApplicationDetailService pwaApplicationDetailService,
                                     UserAccountService userAccountService) {
    this.pipelineDetailService = pipelineDetailService;
    this.asBuiltInteractorService = asBuiltInteractorService;
    this.padProjectInformationService = padProjectInformationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.userAccountService = userAccountService;
  }

  @Override
  public int getExecutionOrder() {
    return 99;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return true;
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {


    var asBuiltPipelineNotificationSpecs = extractAsBuiltPipelineNotificationSpecs(consentWriterDto);

    if (!asBuiltPipelineNotificationSpecs.isEmpty()) {
      var appDetail = pwaApplicationDetailService.getTipDetail(pwaConsent.getSourcePwaApplication());

      // At time of writing this question is hidden for HUOO apps, but they do not ever add pipeline changes to applications.
      // Therefore the possible null value should not be an issue as the asBuilt notification spec list will be empty.
      var latestProjectCompletionDate = padProjectInformationService.getLatestProjectCompletionDate(appDetail)
          .orElseThrow(() ->
              new RuntimeException("Expected to find latest completion date for app but did not. padId:" + appDetail.getId()));
      var deadlineDate = createDefaultAsBuiltGroupDeadlineDate(
          pwaConsent.getConsentInstant(),
          pwaConsent.getSourcePwaApplication().getApplicationType(),
          latestProjectCompletionDate
      );

      asBuiltInteractorService.createAsBuiltNotification(
          pwaConsent,
          pwaConsent.getSourcePwaApplication().getAppReference(),
          deadlineDate,
          userAccountService.getSystemWebUserAccount().getLinkedPerson(),
          asBuiltPipelineNotificationSpecs
      );
    }

    return consentWriterDto;
  }

  private List<AsBuiltPipelineNotificationSpec> extractAsBuiltPipelineNotificationSpecs(ConsentWriterDto consentWriterDto) {

    var pipelinesWithNewDetailAfterConsent = consentWriterDto.getPipelineToNewDetailMap().keySet();

    var pipelineIdToDetailMap = consentWriterDto.getPipelineToNewDetailMap()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> entry.getKey().getPipelineId(), Map.Entry::getValue));

    var pipelineIdToCountOfPipelineDetailMap = pipelineDetailService.countPipelineDetailsPerPipeline(
        pipelinesWithNewDetailAfterConsent);

    return pipelineIdToDetailMap.entrySet()
        .stream()
        // never laid & transferred pipelines do not require as-built notifications
        .filter(pd -> !Set.of(PipelineStatus.NEVER_LAID, PipelineStatus.TRANSFERRED).contains(pd.getValue().getPipelineStatus()))
        .map(entry -> {

          var pipelineId = entry.getKey();
          var pipelineDetail = entry.getValue();

          // Default to standard consent update leaving the pipeline on the seabed.
          var changeCategory = PipelineChangeCategory.CONSENT_UPDATE;

          boolean isNewPipeline = pipelineIdToCountOfPipelineDetailMap.get(pipelineId) == 1L;

          if (isNewPipeline && NEW_PIPELINE_STATUSES.contains(pipelineDetail.getPipelineStatus())) {
            changeCategory = PipelineChangeCategory.NEW_PIPELINE;
          } else if (OUT_OF_USE_OR_RTS_STATUSES.contains(pipelineDetail.getPipelineStatus())) {
            // if it's a pre-existing pipeline that is now out of use or has been removed, set category appropriately
            changeCategory = PipelineChangeCategory.OUT_OF_USE;
          }

          return new AsBuiltPipelineNotificationSpec(pipelineDetail.getPipelineDetailId(), changeCategory);

        })
        .collect(Collectors.toList());
  }

  private LocalDate createDefaultAsBuiltGroupDeadlineDate(Instant consentIssuedDateTime,
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
