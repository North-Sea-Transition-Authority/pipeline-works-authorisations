package uk.co.ogauthority.pwa.service.consultations;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.RouteUtils;

@Service
public class ConsultationFileService {

  private final AppFileService appFileService;
  private final ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CONSULTATION_RESPONSE;

  @Autowired
  public ConsultationFileService(AppFileService appFileService,
                                 ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository) {
    this.appFileService = appFileService;
    this.consultationResponseFileLinkRepository = consultationResponseFileLinkRepository;
  }

  public Map<Integer, List<UploadedFileView>> getConsultationResponseIdToFileViewsMap(PwaApplication pwaApplication,
                                                                               Set<ConsultationResponse> responses) {
    var appFileIdToViewMap = appFileService.getUploadedFileViewsWithNoUrl(pwaApplication, FILE_PURPOSE, ApplicationFileLinkStatus.FULL)
        .stream()
        .collect(Collectors.toMap(UploadedFileView::getFileId, Function.identity()));

    var consultationResponseIdToDocLinksMap = consultationResponseFileLinkRepository.findALlByConsultationResponseIn(responses).stream()
        .collect(Collectors.groupingBy(ConsultationResponseFileLink::getConsultationResponse));

    return consultationResponseIdToDocLinksMap.keySet().stream()
        .collect(Collectors.toMap(
            ConsultationResponse::getId,
            response -> consultationResponseIdToDocLinksMap.getOrDefault(response, List.of()).stream()
                .map(link -> {
                  var fileView = appFileIdToViewMap.get(link.getAppFile().getFileId());
                  fileView.setFileUrl(fileView.getFileId());
                  return fileView;
                })
                .sorted(Comparator.comparing(UploadedFileView::getFileName))
                .collect(toList())
            )
        );
  }

  public String getConsultationFileViewUrl(ConsultationRequest request) {
    var application = request.getPwaApplication();

    return RouteUtils.routeWithUriVariables(on(FILE_PURPOSE.getFileControllerClass()).handleDownload(
        application.getApplicationType(), application.getId(), null, null),
        Map.of("consultationRequestId", request.getId()));
  }

}