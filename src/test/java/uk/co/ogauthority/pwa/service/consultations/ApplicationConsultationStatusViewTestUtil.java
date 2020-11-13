package uk.co.ogauthority.pwa.service.consultations;


import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ApplicationConsultationStatusViewTestUtil {

  private ApplicationConsultationStatusViewTestUtil() {
    // no-instantiation
  }

  public static ApplicationConsultationStatusView noConsultationRequests(){
    return new ApplicationConsultationStatusView(Collections.emptyMap());
  }

  public static ApplicationConsultationStatusView from(List<Pair<ConsultationRequestStatus, Long>> entries){
      var map = entries.stream()
          .collect(toMap(Pair::getLeft, Pair::getRight));
    return new ApplicationConsultationStatusView(map);
  }


}