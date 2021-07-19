package uk.co.ogauthority.pwa.service.testharness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FieldGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PadHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator.PipelineGeneratorService;

@Service
//DELETE - TESTING/DEBUGGING ONLY
public class DebugAppFormGenService {

  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  private final FieldGeneratorService fieldGeneratorService;
  private final PadHuooGeneratorService padHuooGeneratorService;
  private final PipelineGeneratorService pipelineGeneratorService;

  private static final Logger LOGGER = LoggerFactory.getLogger(DebugAppFormGenService.class);


  @Autowired
  public DebugAppFormGenService(
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      PwaApplicationDetailService pwaApplicationDetailService,
      PipelineGeneratorService pipelineGeneratorService,
      FieldGeneratorService fieldGeneratorService,
      PadHuooGeneratorService padHuooGeneratorService) {
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pipelineGeneratorService = pipelineGeneratorService;
    this.fieldGeneratorService = fieldGeneratorService;
    this.padHuooGeneratorService = padHuooGeneratorService;
  }



  public PwaApplicationDetail updateAppForm(Integer detailId, Integer userId, Integer pipelineQuantity) {

    var user = testHarnessUserRetrievalService.getWebUserAccount(userId);
    var pwaApplicationDetail = pwaApplicationDetailService.getDetailById(detailId);

    LOGGER.info("Updating app form section for detail with id: " + pwaApplicationDetail.getId());

    //    fieldGeneratorService.generatePadFields(pwaApplicationDetail);
    //    padHuooGeneratorService.generatePadOrgRoles(user, pwaApplicationDetail);
    pipelineGeneratorService.generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);

    return pwaApplicationDetail;
  }





}
