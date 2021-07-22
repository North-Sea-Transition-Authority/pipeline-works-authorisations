package uk.co.ogauthority.pwa.service.testharness;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;


/**
 *  Service to aid the test harness in pushing a pwa application through the system to a certain stage.
 */
@Service
@Profile("development")
class TestHarnessApplicationStageService {




  void setApplicationStatus(PwaApplicationDetail pwaApplicationDetail,
                            PwaApplicationStatus targetPwaApplicationStatus,
                            Integer assignedCaseOfficerId) {


  }





}
