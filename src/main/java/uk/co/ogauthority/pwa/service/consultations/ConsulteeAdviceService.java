package uk.co.ogauthority.pwa.service.consultations;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeAdviceView;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

@Service
public class ConsulteeAdviceService implements AppProcessingService {

  private final ConsultationResponseService consultationResponseService;
  private final ConsultationResponseDataService consultationResponseDataService;
  private final ConsultationViewService consultationViewService;

  @Autowired
  public ConsulteeAdviceService(ConsultationResponseService consultationResponseService,
                                ConsultationViewService consultationViewService,
                                ConsultationResponseDataService consultationResponseDataService) {
    this.consultationViewService = consultationViewService;
    this.consultationResponseService = consultationResponseService;
    this.consultationResponseDataService = consultationResponseDataService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONSULTEE_ADVICE);
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

    var historicRequestViews = consultationInvolvement.getHistoricalRequests().stream()
        .map(request -> {

          var response = requestToResponseMap.getOrDefault(request, null);

          return consultationViewService.mapConsultationRequestToView(
              request,
              response,
              response != null ? responseToDataMap.get(response) : List.of(),
              consultationInvolvement.getConsulteeGroupDetail());

        })
        .collect(Collectors.toList());

    var activeRequestView = Optional.ofNullable(consultationInvolvement.getActiveRequest())
        .map(r -> consultationViewService
            .mapConsultationRequestToView(r, null, List.of(), consultationInvolvement.getConsulteeGroupDetail()))
        .orElse(null);

    return new ConsulteeAdviceView(consultationInvolvement.getConsulteeGroupDetail().getName(), activeRequestView, historicRequestViews);

  }

}
