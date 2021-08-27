package uk.co.ogauthority.pwa.model.entity.consultations;

import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseDataView;

public class ConsultationRequestViewUtil {

  private ConsultationRequestViewUtil() {
    // no instantiation
  }

  public static ConsultationRequestView createFromRequest(ConsultationRequest consultationRequest,
                                                          ConsultationResponseDocumentType documentType){
    var response = new ConsultationResponse();
    response.setId(10);
    response.setConsultationRequest(consultationRequest);

    var data = new ConsultationResponseData(response);
    data.setResponseGroup(ConsultationResponseOptionGroup.CONTENT);
    data.setResponseType(ConsultationResponseOption.CONFIRMED);
    data.setResponseText("ttt");
    var dataView = ConsultationResponseDataView.from(data);

    return new ConsultationRequestView(
        consultationRequest.getId(),
        "GC name",
        Instant.now(),
        consultationRequest.getStatus(),
        "9 March 2021",
        Instant.now(),
        List.of(dataView),
        false,
        "Response by person",
        List.of(UploadedFileViewTestUtil.createDefaultFileView()),
        "download url",
        documentType);
  }

}
