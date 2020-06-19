package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoMappingService;


@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTechInfoMappingServiceTest {

  private PipelineTechInfoMappingService pipelineTechInfoMappingService;

  @Before
  public void setUp() {
    pipelineTechInfoMappingService = new PipelineTechInfoMappingService();
  }

  private PipelineTechInfoForm createFullForm() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedFieldLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");
    return form;
  }

  private PadPipelineTechInfo createFullEntity() {
    var entity = new PadPipelineTechInfo();
    entity.setEstimatedFieldLife(5);
    entity.setPipelineDesignedToStandards(true);
    entity.setPipelineStandardsDescription("description");
    entity.setCorrosionDescription("description");
    entity.setPlannedPipelineTieInPoints(true);
    entity.setTieInPointsDescription("description");
    return entity;
  }



  @Test
  public void mapEntityToForm_full() {
    var actualForm = new PipelineTechInfoForm();
    var entity = createFullEntity();
    pipelineTechInfoMappingService.mapEntityToForm(actualForm, entity);
    assertThat(actualForm).isEqualTo(createFullForm());
  }

  @Test
  public void mapEntityToForm_partial() {
    var actualForm = new PipelineTechInfoForm();
    var entity = createFullEntity();
    entity.setPipelineDesignedToStandards(false);
    entity.setPlannedPipelineTieInPoints(false);

    pipelineTechInfoMappingService.mapEntityToForm(actualForm, entity);
    var expectedForm = createFullForm();
    expectedForm.setPipelineDesignedToStandards(false);
    expectedForm.setPipelineStandardsDescription(null);
    expectedForm.setPlannedPipelineTieInPoints(false);
    expectedForm.setTieInPointsDescription(null);
    assertThat(actualForm).isEqualTo(expectedForm);
  }


  @Test
  public void mapFormToEntity_full() {
    var actualEntity = new PadPipelineTechInfo();
    var form = createFullForm();
    pipelineTechInfoMappingService.mapFormToEntity(form, actualEntity);
    assertThat(actualEntity).isEqualTo(createFullEntity());
  }

  @Test
  public void mapFormToEntity_partial() {
    var actualEntity = new PadPipelineTechInfo();
    var form = createFullForm();
    form.setPipelineDesignedToStandards(false);
    form.setPlannedPipelineTieInPoints(false);

    pipelineTechInfoMappingService.mapFormToEntity(form, actualEntity);
    var expectedEntity = createFullEntity();
    expectedEntity.setPipelineDesignedToStandards(false);
    expectedEntity.setPipelineStandardsDescription(null);
    expectedEntity.setPlannedPipelineTieInPoints(false);
    expectedEntity.setTieInPointsDescription(null);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }


}