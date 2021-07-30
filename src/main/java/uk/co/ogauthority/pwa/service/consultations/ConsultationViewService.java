package uk.co.ogauthority.pwa.service.consultations;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
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
  private final ConsultationResponseDataService consultationResponseDataService;

  @Autowired
  public ConsultationViewService(
      ConsultationRequestService consultationRequestService,
      ConsultationResponseService consultationResponseService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      TeamManagementService teamManagementService,
      ConsultationResponseDataService consultationResponseDataService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseService = consultationResponseService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.teamManagementService = teamManagementService;
    this.consultationResponseDataService = consultationResponseDataService;
  }


  public List<ConsulteeGroupRequestsView> getConsultationRequestViews(PwaApplication pwaApplication) {
    List<ConsultationRequest> consultationRequests = consultationRequestService.getAllRequestsByApplication(pwaApplication);
    List<ConsulteeGroupRequestsView> consulteeGroupRequestsViews = new ArrayList<>();

    var requestResponseMap = getRequestResponseMap(consultationRequests);
    var responseDataListMap = getResponseDataListMap(requestResponseMap.values());
    var groupAndDetailMap = getGroupAndDetailMap(consultationRequests);
    var groupRequestMap =  getGroupRequestMap(consultationRequests);

    groupRequestMap.forEach((group, requestList) ->
        consulteeGroupRequestsViews.add(createGroupRequestView(requestList, requestResponseMap, responseDataListMap, groupAndDetailMap)));
    consulteeGroupRequestsViews.sort(Comparator.comparing(x -> x.getCurrentRequest().getConsulteeGroupName()));
    return consulteeGroupRequestsViews;
  }


  public ConsultationRequestView getConsultationRequestView(ConsultationRequest consultationRequest) {

    var response = consultationResponseService.getResponseByConsultationRequest(consultationRequest).orElse(null);
    var responseDataList = consultationResponseDataService.findAllByConsultationResponse(response);
    var groupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup());

    return mapConsultationRequestToView(consultationRequest, response, responseDataList, groupDetail);
  }

  public List<ConsultationRequestView> getConsultationRequestViewsRespondedOnly(PwaApplication pwaApplication,
                                                                                ConsultationRequest consultationRequest) {

    List<ConsultationRequest> consultationRequests = consultationRequestService
        .getAllRequestsByAppAndGroupRespondedOnly(pwaApplication, consultationRequest.getConsulteeGroup());
    List<ConsultationRequestView> consultationRequestViews = new ArrayList<>();

    var requestResponseMap = getRequestResponseMap(consultationRequests);

    var responseDataListMap = getResponseDataListMap(requestResponseMap.values());

    var groupDetail = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup());

    requestResponseMap.forEach((request, response) ->
        consultationRequestViews.add(mapConsultationRequestToView(request, response, responseDataListMap.get(response), groupDetail)));

    consultationRequestViews.sort(Comparator.comparing(ConsultationRequestView::getResponseDate).reversed());

    return consultationRequestViews;

  }

  private Map<ConsultationResponse, List<ConsultationResponseData>> getResponseDataListMap(Collection<ConsultationResponse> responses) {
    return consultationResponseDataService.findAllByConsultationResponseIn(responses).stream()
        .collect(Collectors.groupingBy(ConsultationResponseData::getConsultationResponse));
  }

  private ConsulteeGroupRequestsView createGroupRequestView(List<ConsultationRequest> requestList,
                                                            Map<ConsultationRequest, ConsultationResponse> requestResponseMap,
                                                            Map<ConsultationResponse, List<ConsultationResponseData>> responseDataListMap,
                                                            Map<ConsulteeGroup, ConsulteeGroupDetail> groupAndDetailMap) {
    var consulteeGroupRequestsView = new ConsulteeGroupRequestsView();
    for (int requestIndex = 0; requestIndex < requestList.size(); requestIndex++) {

      var consultationRequest = requestList.get(requestIndex);
      var response = requestResponseMap.get(consultationRequest);

      var consultationRequestView = mapConsultationRequestToView(
          consultationRequest,
          response,
          responseDataListMap.get(response),
          groupAndDetailMap.get(consultationRequest.getConsulteeGroup())
      );

      if (requestIndex == 0) {
        consulteeGroupRequestsView.setCurrentRequest(consultationRequestView);
      } else {
        consulteeGroupRequestsView.addHistoricalRequest(consultationRequestView);
      }

    }

    Comparator<ConsultationRequestView> requestViewComparator
        = Comparator.comparing(ConsultationRequestView::getRequestDate).reversed();
    consulteeGroupRequestsView.getHistoricalRequests().sort(requestViewComparator);
    return consulteeGroupRequestsView;
  }

  public ConsultationRequestView mapConsultationRequestToView(ConsultationRequest consultationRequest,
                                                              ConsultationResponse consultationResponse,
                                                              List<ConsultationResponseData> consultationResponseDataList,
                                                              ConsulteeGroupDetail consulteeGroupDetail) {

    var responseDataViews = Optional.ofNullable(consultationResponseDataList)
        .orElse(List.of())
        .stream()
        .map(ConsultationResponseDataView::from)
        .collect(Collectors.toList());

    if (consultationResponse != null) {
      return new ConsultationRequestView(
          consultationRequest.getId(),
          consulteeGroupDetail.getName(),
          consultationRequest.getStartTimestamp(),
          consultationRequest.getStatus(),
          DateUtils.formatDateTime(consultationRequest.getDeadlineDate().truncatedTo(ChronoUnit.SECONDS)),
          consultationResponse.getResponseTimestamp(),
          responseDataViews,
          false,
          teamManagementService.getPerson(consultationResponse.getRespondingPersonId()).getFullName());

    } else { //awaiting response or withdrawn
      return new ConsultationRequestView(
          consultationRequest.getId(),
          consulteeGroupDetail.getName(),
          consultationRequest.getStartTimestamp(),
          consultationRequest.getStatus(),
          DateUtils.formatDateTime(consultationRequest.getDeadlineDate().truncatedTo(ChronoUnit.SECONDS)),
          responseDataViews,
          consultationRequest.getStatus() != ConsultationRequestStatus.WITHDRAWN,
          consultationRequest.getStatus() == ConsultationRequestStatus.WITHDRAWN
              ? teamManagementService.getPerson(consultationRequest.getEndedByPersonId()).getFullName() : null,
          consultationRequest.getStatus() == ConsultationRequestStatus.WITHDRAWN
              ? DateUtils.formatDateTime(consultationRequest.getEndTimestamp().truncatedTo(ChronoUnit.SECONDS)) : null);
    }
  }

  private Map<ConsultationRequest, ConsultationResponse> getRequestResponseMap(List<ConsultationRequest> consultationRequests) {
    List<ConsultationResponse> consultationResponses = consultationResponseService.getResponsesByConsultationRequests(consultationRequests);
    Map<ConsultationRequest, ConsultationResponse> requestResponseMap = new LinkedHashMap<>();
    consultationResponses.forEach(consultationResponse ->
        requestResponseMap.put(consultationResponse.getConsultationRequest(), consultationResponse));
    return requestResponseMap;
  }

  private Map<ConsulteeGroup, ConsulteeGroupDetail> getGroupAndDetailMap(List<ConsultationRequest> consultationRequests) {
    Set<ConsulteeGroup> consulteeGroups = new HashSet<>();
    consultationRequests.forEach(request -> consulteeGroups.add(request.getConsulteeGroup()));
    var consulteeGroupDetails = consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(consulteeGroups);
    return consulteeGroupDetails.stream()
        .collect(Collectors.toMap(ConsulteeGroupDetail::getConsulteeGroup, Function.identity()));
  }

  private Map<ConsulteeGroup, List<ConsultationRequest>> getGroupRequestMap(List<ConsultationRequest> consultationRequests) {
    return  consultationRequests.stream()
        .collect(groupingBy(ConsultationRequest::getConsulteeGroup, toList()));
  }


}
