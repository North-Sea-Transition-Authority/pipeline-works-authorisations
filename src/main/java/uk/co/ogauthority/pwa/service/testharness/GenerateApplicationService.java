package uk.co.ogauthority.pwa.service.testharness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.BlocksAndCrossingsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.DesignOpConditionsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.EnvAndDecomGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FastTrackGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FieldGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FluidCompositionGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.GeneralTechDetailsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.LocationDetailsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.OtherPropertiesGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PadHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PartnerLettersGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PipelineHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PipelineSchematicsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.ProjectInformationGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator.PipelineGeneratorService;

@Service
@Profile("development")
public class GenerateApplicationService {

  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;

  private final FieldGeneratorService fieldGeneratorService;
  private final ProjectInformationGeneratorService projectInformationGeneratorService;
  private final FastTrackGeneratorService fastTrackGeneratorService;
  private final EnvAndDecomGeneratorService envAndDecomGeneratorService;
  private final PadHuooGeneratorService padHuooGeneratorService;
  private final PartnerLettersGeneratorService partnerLettersGeneratorService;
  private final LocationDetailsGeneratorService locationDetailsGeneratorService;
  private final BlocksAndCrossingsGeneratorService blocksAndCrossingsGeneratorService;
  private final GeneralTechDetailsGeneratorService generalTechDetailsGeneratorService;
  private final FluidCompositionGeneratorService fluidCompositionGeneratorService;
  private final OtherPropertiesGeneratorService otherPropertiesGeneratorService;
  private final DesignOpConditionsGeneratorService designOpConditionsGeneratorService;
  private final PipelineGeneratorService pipelineGeneratorService;
  private final PipelineSchematicsGeneratorService pipelineSchematicsGeneratorService;
  private final PipelineHuooGeneratorService pipelineHuooGeneratorService;

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateApplicationService.class);


  @Autowired
  public GenerateApplicationService(
      PwaApplicationCreationService pwaApplicationCreationService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      FieldGeneratorService fieldGeneratorService,
      ProjectInformationGeneratorService projectInformationGeneratorService,
      FastTrackGeneratorService fastTrackGeneratorService,
      EnvAndDecomGeneratorService envAndDecomGeneratorService,
      PadHuooGeneratorService padHuooGeneratorService,
      PartnerLettersGeneratorService partnerLettersGeneratorService,
      LocationDetailsGeneratorService locationDetailsGeneratorService,
      BlocksAndCrossingsGeneratorService blocksAndCrossingsGeneratorService,
      GeneralTechDetailsGeneratorService generalTechDetailsGeneratorService,
      FluidCompositionGeneratorService fluidCompositionGeneratorService,
      OtherPropertiesGeneratorService otherPropertiesGeneratorService,
      DesignOpConditionsGeneratorService designOpConditionsGeneratorService,
      PipelineGeneratorService pipelineGeneratorService,
      PipelineSchematicsGeneratorService pipelineSchematicsGeneratorService,
      PipelineHuooGeneratorService pipelineHuooGeneratorService) {
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.fieldGeneratorService = fieldGeneratorService;
    this.projectInformationGeneratorService = projectInformationGeneratorService;
    this.fastTrackGeneratorService = fastTrackGeneratorService;
    this.envAndDecomGeneratorService = envAndDecomGeneratorService;
    this.padHuooGeneratorService = padHuooGeneratorService;
    this.partnerLettersGeneratorService = partnerLettersGeneratorService;
    this.locationDetailsGeneratorService = locationDetailsGeneratorService;
    this.blocksAndCrossingsGeneratorService = blocksAndCrossingsGeneratorService;
    this.generalTechDetailsGeneratorService = generalTechDetailsGeneratorService;
    this.fluidCompositionGeneratorService = fluidCompositionGeneratorService;
    this.otherPropertiesGeneratorService = otherPropertiesGeneratorService;
    this.designOpConditionsGeneratorService = designOpConditionsGeneratorService;
    this.pipelineGeneratorService = pipelineGeneratorService;
    this.pipelineSchematicsGeneratorService = pipelineSchematicsGeneratorService;
    this.pipelineHuooGeneratorService = pipelineHuooGeneratorService;
  }




  PwaApplicationDetail generateInitialPwaApplication(Integer pipelineQuantity, Integer applicantPersonId) {

    var user = testHarnessUserRetrievalService.getWebUserAccount(applicantPersonId);
    var pwaApplicationDetail = pwaApplicationCreationService.createInitialPwaApplication(user);

    LOGGER.info("Application detail created with id: {} and app ref: {}",
        pwaApplicationDetail.getId(), pwaApplicationDetail.getPwaApplicationRef());
    LOGGER.info("Generating app form sections for detail with id: {}", pwaApplicationDetail.getId());

    fieldGeneratorService.generatePadFields(pwaApplicationDetail);
    projectInformationGeneratorService.generateProjectInformation(user, pwaApplicationDetail);
    fastTrackGeneratorService.generateFastTrack(pwaApplicationDetail);
    envAndDecomGeneratorService.generateEnvAndDecom(pwaApplicationDetail);
    padHuooGeneratorService.generatePadOrgRoles(user, pwaApplicationDetail);
    partnerLettersGeneratorService.generatePartnerLetters(user, pwaApplicationDetail);
    locationDetailsGeneratorService.generateLocationDetails(pwaApplicationDetail);
    blocksAndCrossingsGeneratorService.generateBlocksAndCrossings(pwaApplicationDetail);
    generalTechDetailsGeneratorService.generateGeneralTechDetails(pwaApplicationDetail);
    fluidCompositionGeneratorService.generateFluidComposition(pwaApplicationDetail);
    otherPropertiesGeneratorService.generateOtherProperties(pwaApplicationDetail);
    designOpConditionsGeneratorService.generateDesignOpConditions(pwaApplicationDetail);
    pipelineGeneratorService.generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);
    pipelineSchematicsGeneratorService.generatePipelineSchematics(user, pwaApplicationDetail);
    pipelineHuooGeneratorService.generatePipelineHuoos(pwaApplicationDetail);


    LOGGER.info("App form sections generated successfully for detail with id: {}", pwaApplicationDetail.getId());

    return pwaApplicationDetail;
  }





}
