package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;

@Service
@Profile("development")
public class EnvAndDecomGeneratorService {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;


  @Autowired
  public EnvAndDecomGeneratorService(
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService) {
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
  }



  public void generateEnvAndDecom(PwaApplicationDetail pwaApplicationDetail) {

    var padEnvDecom = new PadEnvironmentalDecommissioning();
    setEnvAndDecomData(pwaApplicationDetail, padEnvDecom);
    padEnvironmentalDecommissioningService.save(padEnvDecom);
  }


  private void setEnvAndDecomData(PwaApplicationDetail pwaApplicationDetail,
                                  PadEnvironmentalDecommissioning padEnvDecom) {

    var requiredQuestions = padEnvironmentalDecommissioningService.getAvailableQuestions(pwaApplicationDetail);

    padEnvDecom.setPwaApplicationDetail(pwaApplicationDetail);

    if (requiredQuestions.contains(EnvDecomQuestion.TRANS_BOUNDARY)) {
      padEnvDecom.setTransboundaryEffect(true);
    }

    if (requiredQuestions.contains(EnvDecomQuestion.BEIS_EMT_PERMITS)) {
      padEnvDecom.setEmtHasSubmittedPermits(true);
      padEnvDecom.setEmtHasOutstandingPermits(false);
      padEnvDecom.setPermitsSubmitted("My submitted permits");
    }

    if (requiredQuestions.contains(EnvDecomQuestion.ACKNOWLEDGEMENTS)) {
      padEnvDecom.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    }

    if (requiredQuestions.contains(EnvDecomQuestion.DECOMMISSIONING)) {
      padEnvDecom.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    }

  }




}
