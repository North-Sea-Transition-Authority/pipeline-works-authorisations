package uk.co.ogauthority.pwa.service.consultations;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.util.DateUtils;

/*
 A service to asks consultation request and response services for consultation group data for an application
 and construct view objects for templates.
 */
@Service
public class ConsultationViewService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseService consultationResponseService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final TeamManagementService teamManagementService;

  @Autowired
  public ConsultationViewService(
      ConsultationRequestService consultationRequestService,
      ConsultationResponseService consultationResponseService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      TeamManagementService teamManagementService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseService = consultationResponseService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.teamManagementService = teamManagementService;
  }




  public List<ConsulteeGroupRequestsView> getConsultationRequestViews(PwaApplication pwaApplication) {
    List<ConsultationRequest> consultationRequests = consultationRequestService.getAllRequestsByApplication(pwaApplication);
    List<ConsulteeGroupRequestsView> consulteeGroupRequestsViews = new ArrayList<>();

    Map<ConsulteeGroup, List<ConsultationRequest>> requestMap =  consultationRequests.stream()
        .collect(groupingBy(ConsultationRequest::getConsulteeGroup, toList()));

    requestMap.forEach((group, requestList) -> {
      consulteeGroupRequestsViews.add(createGroupRequestView(requestList));
    });

    consulteeGroupRequestsViews.sort(Comparator.comparing(x -> x.getCurrentRequest().getConsulteeGroupName()));
    return consulteeGroupRequestsViews;
  }

  private ConsulteeGroupRequestsView createGroupRequestView(List<ConsultationRequest> requestList) {
    var consulteeGroupRequestsView = new ConsulteeGroupRequestsView();
    for (int requestIndex = 0; requestIndex < requestList.size(); requestIndex++) {
      var consultationRequestView = mapConsultationRequestToView(requestList.get(requestIndex));
      if (requestIndex == 0) {
        consulteeGroupRequestsView.setCurrentRequest(consultationRequestView);
      } else {
        consulteeGroupRequestsView.addHistoricalRequest(consultationRequestView);
      }
    }
    return consulteeGroupRequestsView;
  }

  private ConsultationRequestView mapConsultationRequestToView(ConsultationRequest consultationRequest) {
    var consulteeGroupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroup(consultationRequest.getConsulteeGroup());

    var consultationResponse = consultationResponseService.getResponseByConsultationRequest(consultationRequest);
    if (consultationResponse.isPresent()) {
      return new ConsultationRequestView(
          consulteeGroupDetail.getName(),
          DateUtils.formatDateTime(consultationRequest.getStartTimestamp().truncatedTo(ChronoUnit.SECONDS)),
          consultationRequest.getStatus(),
          DateUtils.formatDateTime(consultationRequest.getDeadlineDate().truncatedTo(ChronoUnit.SECONDS)),
          consultationResponse.get().getResponseType(),
          teamManagementService.getPerson(consultationResponse.get().getRespondingPersonId()).getFullName(),
          consultationResponse.get().getResponseType().equals(ConsultationResponseOption.REJECTED)
              ? consultationResponse.get().getResponseText() : null);

    } else {
      return new ConsultationRequestView(
          consulteeGroupDetail.getName(),
          DateUtils.formatDateTime(consultationRequest.getStartTimestamp().truncatedTo(ChronoUnit.SECONDS)),
          consultationRequest.getStatus(),
          DateUtils.formatDateTime(consultationRequest.getDeadlineDate().truncatedTo(ChronoUnit.SECONDS)));
    }
  }


}
