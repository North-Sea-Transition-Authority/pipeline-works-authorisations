package uk.co.ogauthority.pwa.service.consultations;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeAdviceView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;

@Service
public class ConsulteeAdviceService implements AppProcessingService {

  private final ConsultationResponseService consultationResponseService;
  private final ConsultationResponseDataService consultationResponseDataService;
  private final ConsultationViewService consultationViewService;
  private final ConsultationFileService consultationFileService;

  @Autowired
  public ConsulteeAdviceService(ConsultationResponseService consultationResponseService,
                                ConsultationViewService consultationViewService,
                                ConsultationResponseDataService consultationResponseDataService,
                                ConsultationFileService consultationFileService) {
    this.consultationViewService = consultationViewService;
    this.consultationResponseService = consultationResponseService;
    this.consultationResponseDataService = consultationResponseDataService;
    this.consultationFileService = consultationFileService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return !ApplicationState.ENDED.includes(processingContext.getApplicationDetailStatus())
      && processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONSULTEE_ADVICE);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    return new TaskListEntry(
        PwaAppProcessingTask.CONSULTEE_ADVICE.getTaskName(),
        PwaAppProcessingTask.CONSULTEE_ADVICE.getRoute(processingContext),
        null,
        PwaAppProcessingTask.CONSULTEE_ADVICE.getDisplayOrder()
    );

  }

  public ConsulteeAdviceView getConsulteeAdviceView(PwaAppProcessingContext processingContext) {

    var consultationInvolvement = processingContext.getApplicationInvolvement().getConsultationInvolvement()
        .orElseThrow(() -> new RuntimeException(String.format(
            "Couldn't find consultation involvement for app with ID: %s",
            processingContext.getMasterPwaApplicationId())));

    var requestToResponseMap = consultationResponseService
        .getResponsesByConsultationRequests(consultationInvolvement.getHistoricalRequests())
        .stream()
        .collect(Collectors.toMap(ConsultationResponse::getConsultationRequest, Function.identity()));

    var responseToDataMap = consultationResponseDataService
        .findAllByConsultationResponseIn(requestToResponseMap.values())
        .stream()
        .collect(Collectors.groupingBy(ConsultationResponseData::getConsultationResponse));

    var consultationResponseIdToFileViewsMap = consultationFileService.getConsultationResponseIdToFileViewsMap(
        processingContext.getPwaApplication(), new HashSet<>(requestToResponseMap.values()));

    var historicRequestViews = consultationInvolvement.getHistoricalRequests().stream()
        .map(request -> {

          var response = requestToResponseMap.getOrDefault(request, null);

          var downloadFileUrl = consultationFileService.getConsultationFileViewUrl(request);

          return consultationViewService.mapConsultationRequestToView(
              request,
              response,
              response != null ? responseToDataMap.get(response) : List.of(),
              consultationInvolvement.getConsulteeGroupDetail(),
              consultationResponseIdToFileViewsMap.getOrDefault(Objects.nonNull(response) ? response.getId() : null, List.of()),
              downloadFileUrl);

        })
        .collect(Collectors.toList());

    var activeRequestView = Optional.ofNullable(consultationInvolvement.getActiveRequest())
        .map(r -> {
          var downloadFileUrl = consultationFileService.getConsultationFileViewUrl(r);

          return consultationViewService.mapConsultationRequestToView(r, null, List.of(),
              consultationInvolvement.getConsulteeGroupDetail(), List.of(), downloadFileUrl);
        })
        .orElse(null);

    return new ConsulteeAdviceView(consultationInvolvement.getConsulteeGroupDetail().getName(), activeRequestView, historicRequestViews);

  }

}
