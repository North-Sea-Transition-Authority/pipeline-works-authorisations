package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineFlexibility;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@Service
@Profile("test-harness")
class PipelineGeneratorService implements TestHarnessAppFormService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadPipelineIdentDataService padPipelineIdentDataService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PIPELINES;


  @Autowired
  public PipelineGeneratorService(
      PadPipelineService padPipelineService,
      PadPipelineIdentService padPipelineIdentService,
      PadPipelineIdentDataService padPipelineIdentDataService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padPipelineIdentDataService = padPipelineIdentDataService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    for (var x = 0; x < appFormServiceParams.getPipelineQuantity(); x++) {

      var pipelineHeaderForm = createPadPipelineForm();
      var padPipeline = padPipelineService.addPipeline(appFormServiceParams.getApplicationDetail(), pipelineHeaderForm);

      generateIdents(padPipeline);
    }
  }

  private PipelineHeaderForm createPadPipelineForm() {

    var form = new PipelineHeaderForm();
    form.setPipelineType(TestHarnessAppFormUtil.getRandomPipelineType());

    form.setFromLocation(TestHarnessAppFormUtil.getRandomPipelineLocation());
    form.setFromCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());
    form.setToLocation(TestHarnessAppFormUtil.getRandomPipelineLocation());
    form.setToCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());

    form.setComponentPartsDescription("This is the component parts description");
    form.setLength(BigDecimal.valueOf(495));
    form.setProductsToBeConveyed("Corrosion Inhibitors");
    form.setTrenchedBuriedBackfilled(false);
    form.setPipelineFlexibility(PipelineFlexibility.FLEXIBLE);
    form.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    form.setPipelineDesignLife(100);
    form.setPipelineInBundle(false);
    form.setAlreadyExistsOnSeabed(true);
    form.setPipelineInUse(true);
    form.setFootnote("This is the footnote description");

    return form;
  }


  private void generateIdents(PadPipeline padPipeline) {

    var totalIdents = RandomUtils.nextInt(1, 11);

    for (var y = 0; y < totalIdents; y++) {
      var identForm = createIdentForm(padPipeline, y + 1, totalIdents);
      setIdentDataFormData(padPipeline, identForm.getDataForm());
      padPipelineIdentService.addIdent(padPipeline, identForm);
    }
  }


  private PipelineIdentForm createIdentForm(PadPipeline padPipeline, int identNumber, int totalIdents) {

    var identForm = new PipelineIdentForm();

    //complies with validation rule for the first and last ident matching with the header location and coordinates
    if (identNumber == 1) {
      identForm.setFromLocation(padPipeline.getFromLocation());
      identForm.setFromCoordinateForm(TestHarnessAppFormUtil.getCoordinateFormFromPair(padPipeline.getFromCoordinates()));
    } else {
      identForm.setFromLocation(TestHarnessAppFormUtil.getRandomPipelineLocation());
      identForm.setFromCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());
    }

    if (identNumber == totalIdents) {
      identForm.setToLocation(padPipeline.getToLocation());
      identForm.setToCoordinateForm(TestHarnessAppFormUtil.getCoordinateFormFromPair(padPipeline.getToCoordinates()));
    } else {
      identForm.setToLocation(TestHarnessAppFormUtil.getRandomPipelineLocation());
      identForm.setToCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());
    }

    //complies with validation rule for total ident length matching header length
    var headerLength = padPipeline.getLength();
    var eachIdentLength = headerLength.divide(BigDecimal.valueOf(totalIdents), new MathContext(2));
    var totalIdentLength = eachIdentLength.multiply(BigDecimal.valueOf(totalIdents));
    identForm.setLength(new DecimalInput(eachIdentLength));

    if (identNumber == totalIdents
        && totalIdentLength.compareTo(headerLength) != 0) {
      var remainingLength = headerLength.subtract(totalIdentLength);
      identForm.setLength(new DecimalInput(eachIdentLength.add(remainingLength)));
    }

    identForm.setDefiningStructure(false);
    identForm.setDataForm(new PipelineIdentDataForm());

    return identForm;
  }


  private void setIdentDataFormData(PadPipeline padPipeline, PipelineIdentDataForm identDataForm) {

    identDataForm.setComponentPartsDescription("This is the component parts description");
    if (padPipeline.getCoreType().equals(PipelineCoreType.SINGLE_CORE)) {
      identDataForm.setExternalDiameter(new DecimalInput(BigDecimal.valueOf(RandomUtils.nextInt(10, 101))));
      identDataForm.setInternalDiameter(new DecimalInput(
          identDataForm.getExternalDiameter().createBigDecimalOrNull().subtract(BigDecimal.ONE)));
      identDataForm.setWallThickness(new DecimalInput(BigDecimal.TEN));
      identDataForm.setMaop(new DecimalInput(BigDecimal.TEN));
      identDataForm.setInsulationCoatingType("coating type");
      identDataForm.setProductsToBeConveyed("description");
      identDataForm.setExternalDiameterMultiCore(null);
      identDataForm.setInternalDiameterMultiCore(null);
      identDataForm.setWallThicknessMultiCore(null);
      identDataForm.setMaopMultiCore(null);
      identDataForm.setInsulationCoatingTypeMultiCore(null);
      identDataForm.setProductsToBeConveyedMultiCore(null);

    } else {
      identDataForm.setExternalDiameterMultiCore("external diameter");
      identDataForm.setInternalDiameterMultiCore("internal diameter");
      identDataForm.setWallThicknessMultiCore("Thickness");
      identDataForm.setMaopMultiCore("maop");
      identDataForm.setInsulationCoatingTypeMultiCore("Coating type");
      identDataForm.setProductsToBeConveyedMultiCore("description");
      identDataForm.setExternalDiameter(null);
      identDataForm.setInternalDiameter(null);
      identDataForm.setWallThickness(null);
      identDataForm.setMaop(null);
      identDataForm.setInsulationCoatingType(null);
      identDataForm.setProductsToBeConveyed(null);
    }
  }



}
