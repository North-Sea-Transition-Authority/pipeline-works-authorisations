package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinePersisterService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;

@Service
@Profile("development")
public class PipelineGeneratorService {

  private final PipelineService pipelineService;
  private final PadPipelineRepository padPipelineRepository;
  private final PadPipelinePersisterService padPipelinePersisterService;
  private final PadPipelineIdentRepository padPipelineIdentRepository;
  private final PadPipelineIdentDataRepository padPipelineIdentDataRepository;


  @Autowired
  public PipelineGeneratorService(
      PipelineService pipelineService,
      PadPipelineRepository padPipelineRepository,
      PadPipelinePersisterService padPipelinePersisterService,
      PadPipelineIdentRepository padPipelineIdentRepository,
      PadPipelineIdentDataRepository padPipelineIdentDataRepository) {
    this.pipelineService = pipelineService;
    this.padPipelineRepository = padPipelineRepository;
    this.padPipelinePersisterService = padPipelinePersisterService;
    this.padPipelineIdentRepository = padPipelineIdentRepository;
    this.padPipelineIdentDataRepository = padPipelineIdentDataRepository;
  }


  public void generatePadPipelinesAndIdents(PwaApplicationDetail pwaApplicationDetail, Integer pipelineQuantity) {

    for (var x = 0; x < pipelineQuantity; x++) {

      var pipeline = pipelineService.createApplicationPipeline(pwaApplicationDetail.getPwaApplication());
      var padPipeline = new PadPipeline(pwaApplicationDetail);
      padPipeline.setPipeline(pipeline);
      padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

      Integer maxTemporaryNumber = padPipelineRepository.getMaxTemporaryNumberByPwaApplicationDetail(
          pwaApplicationDetail);

      padPipeline.setTemporaryNumber(maxTemporaryNumber + 1);
      padPipeline.setPipelineRef("TEMPORARY " + padPipeline.getTemporaryNumber());
      setPadPipelineData(padPipeline);
      padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);

      generateIdents(padPipeline);
    }
  }

  private void setPadPipelineData(PadPipeline padPipeline) {
    padPipeline.setPipelineType(PipelineTestHarnessUtil.getRandomPipelineType());

    padPipeline.setFromLocation(PipelineTestHarnessUtil.getRandomPipelineLocation());
    padPipeline.setFromCoordinates(PipelineTestHarnessUtil.getRandomCoordinates());
    padPipeline.setToLocation(PipelineTestHarnessUtil.getRandomPipelineLocation());
    padPipeline.setToCoordinates(PipelineTestHarnessUtil.getRandomCoordinates());

    padPipeline.setComponentPartsDescription("This is the component parts description");
    padPipeline.setLength(BigDecimal.valueOf(495));
    padPipeline.setProductsToBeConveyed("Corrosion Inhibitors");
    padPipeline.setTrenchedBuriedBackfilled(false);
    padPipeline.setPipelineFlexibility(PipelineFlexibility.FLEXIBLE);
    padPipeline.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    padPipeline.setPipelineDesignLife(100);
    padPipeline.setPipelineInBundle(false);
    padPipeline.setAlreadyExistsOnSeabed(true);
    padPipeline.setPipelineInUse(true);
    padPipeline.setFootnote("This is the footnote description");
  }


  private void generateIdents(PadPipeline padPipeline) {

    var totalIdents = RandomUtils.nextInt(1, 11);
    var idents = new ArrayList<PadPipelineIdent>();
    var identDataEntities = new ArrayList<PadPipelineIdentData>();

    for (var y = 0; y < totalIdents; y++) {
      var ident = new PadPipelineIdent(padPipeline, y + 1);
      setIdentBasicData(ident, totalIdents);
      idents.add(ident);

      var identData = new PadPipelineIdentData(ident);
      createIdentData(identData);
      identDataEntities.add(identData);
    }

    padPipelineIdentRepository.saveAll(idents);
    padPipelineIdentDataRepository.saveAll(identDataEntities);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
  }


  private void setIdentBasicData(PadPipelineIdent ident, int totalIdents) {

    //complies with validation rule for the first and last ident matching with the header location and coordinates
    if (ident.getIdentNo() == 1) {
      ident.setFromLocation(ident.getPadPipeline().getFromLocation());
      ident.setFromCoordinates(ident.getPadPipeline().getFromCoordinates());
    } else {
      ident.setFromLocation(PipelineTestHarnessUtil.getRandomPipelineLocation());
      ident.setFromCoordinates(PipelineTestHarnessUtil.getRandomCoordinates());
    }

    if (ident.getIdentNo() == totalIdents) {
      ident.setToLocation(ident.getPadPipeline().getToLocation());
      ident.setToCoordinates(ident.getPadPipeline().getToCoordinates());
    } else {
      ident.setToLocation(PipelineTestHarnessUtil.getRandomPipelineLocation());
      ident.setToCoordinates(PipelineTestHarnessUtil.getRandomCoordinates());
    }

    //complies with validation rule for total ident length matching header length
    var headerLength = ident.getPadPipeline().getLength();
    var eachIdentLength = headerLength.divide(BigDecimal.valueOf(totalIdents), new MathContext(2));
    var totalIdentLength = eachIdentLength.multiply(BigDecimal.valueOf(totalIdents));
    ident.setLength(eachIdentLength);

    if (ident.getIdentNo() == totalIdents
        && totalIdentLength.compareTo(headerLength) != 0) {
      var remainingLength = headerLength.subtract(totalIdentLength);
      ident.setLength(eachIdentLength.add(remainingLength));
    }

    ident.setDefiningStructure(false);
  }


  private PadPipelineIdentData createIdentData(PadPipelineIdentData identData) {

    identData.setComponentPartsDesc("This is the component parts description");
    if (identData.getPadPipelineIdent().getPadPipeline().getCoreType().equals(
        PipelineCoreType.SINGLE_CORE)) {
      identData.setExternalDiameter(BigDecimal.valueOf(RandomUtils.nextInt(10, 101)));
      identData.setInternalDiameter(identData.getExternalDiameter().subtract(BigDecimal.ONE));
      identData.setWallThickness(BigDecimal.TEN);
      identData.setMaop(BigDecimal.TEN);
      identData.setInsulationCoatingType("coating type");
      identData.setProductsToBeConveyed("description");
      identData.setExternalDiameterMultiCore(null);
      identData.setInternalDiameterMultiCore(null);
      identData.setWallThicknessMultiCore(null);
      identData.setMaopMultiCore(null);
      identData.setInsulationCoatingTypeMultiCore(null);
      identData.setProductsToBeConveyedMultiCore(null);

    } else {
      identData.setExternalDiameterMultiCore("external diameter");
      identData.setInternalDiameterMultiCore("internal diameter");
      identData.setWallThicknessMultiCore("Thickness");
      identData.setMaopMultiCore("maop");
      identData.setInsulationCoatingTypeMultiCore("Coating type");
      identData.setProductsToBeConveyedMultiCore("description");
      identData.setExternalDiameter(null);
      identData.setInternalDiameter(null);
      identData.setWallThickness(null);
      identData.setMaop(null);
      identData.setInsulationCoatingType(null);
      identData.setProductsToBeConveyed(null);
    }

    return identData;
  }



}
