package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.asbuilt;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationSpec;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriter;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

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
      var deadlineDate = asBuiltInteractorService.createDefaultAsBuiltGroupDeadlineDate(
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

  @VisibleForTesting
  List<AsBuiltPipelineNotificationSpec> extractAsBuiltPipelineNotificationSpecs(ConsentWriterDto consentWriterDto) {

    var pipelinesWithNewDetailAfterConsent = consentWriterDto.getPipelineToNewDetailMap().keySet();

    var pipelineIdToDetailMap = consentWriterDto.getPipelineToNewDetailMap()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> entry.getKey().getPipelineId(), Map.Entry::getValue));

    var pipelineIdToCountOfPipelineDetailMap = pipelineDetailService.countPipelineDetailsPerPipeline(
        pipelinesWithNewDetailAfterConsent);

    return pipelineIdToDetailMap.entrySet()
        .stream()
        // never laid pipelines do not require as-built notifications
        .filter(pd -> !PipelineStatus.NEVER_LAID.equals(pd.getValue().getPipelineStatus()))
        .map(entry -> {
          var pipelineId = entry.getKey();
          var pipelineDetail = entry.getValue();

          var outOfUseOrReturnedToShorePipelineStatuses = Set.of(
              PipelineStatus.OUT_OF_USE_ON_SEABED, PipelineStatus.RETURNED_TO_SHORE
          );

          // Default to standard consent update leaving the pipeline on the seabed.
          var changeCategory = PipelineChangeCategory.CONSENT_UPDATE;
          if (outOfUseOrReturnedToShorePipelineStatuses.contains(pipelineDetail.getPipelineStatus())) {
            changeCategory = PipelineChangeCategory.OUT_OF_USE;
            // If no other case hit and the pipeline has only a single pipeline detail, then the pipeline must be newly consented.
          } else if (pipelineIdToCountOfPipelineDetailMap.get(pipelineId) == 1L) {
            changeCategory = PipelineChangeCategory.NEW_PIPELINE;
          }

          return new AsBuiltPipelineNotificationSpec(pipelineDetail.getPipelineDetailId(), changeCategory);
        })
        .collect(Collectors.toList());
  }
}
