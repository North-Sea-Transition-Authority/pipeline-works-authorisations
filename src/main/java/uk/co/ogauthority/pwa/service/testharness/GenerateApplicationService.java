package uk.co.ogauthority.pwa.service.testharness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FieldGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PadHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator.PipelineGeneratorService;

@Service
public class GenerateApplicationService {

  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;

  private final FieldGeneratorService fieldGeneratorService;
  private final PadHuooGeneratorService padHuooGeneratorService;
  private final PipelineGeneratorService pipelineGeneratorService;

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateApplicationService.class);


  @Autowired
  public GenerateApplicationService(
      PwaApplicationCreationService pwaApplicationCreationService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      FieldGeneratorService fieldGeneratorService,
      PadHuooGeneratorService padHuooGeneratorService,
      PipelineGeneratorService pipelineGeneratorService) {
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.pipelineGeneratorService = pipelineGeneratorService;
    this.fieldGeneratorService = fieldGeneratorService;
    this.padHuooGeneratorService = padHuooGeneratorService;
  }



  PwaApplicationDetail generateInitialPwaApplication(Integer pipelineQuantity, Integer applicantPersonId) {

    var user = testHarnessUserRetrievalService.getWebUserAccount(applicantPersonId);
    var pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(user);

    LOGGER.info("Application detail created with id: " + pwaApplicationDetail.getId());
    LOGGER.info("Generating app form sections for detail with id: " + pwaApplicationDetail.getId());

    fieldGeneratorService.generatePadFields(pwaApplicationDetail);
    padHuooGeneratorService.generatePadOrgRoles(user, pwaApplicationDetail);
    pipelineGeneratorService.generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);

    return pwaApplicationDetail;
  }





}
