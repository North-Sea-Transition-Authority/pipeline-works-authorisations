package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PipelineTechInfoMappingServiceTest {

  private PipelineTechInfoMappingService pipelineTechInfoMappingService;

  @BeforeEach
  void setUp() {
    pipelineTechInfoMappingService = new PipelineTechInfoMappingService();
  }

  private PipelineTechInfoForm createFullForm() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedAssetLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");
    return form;
  }

  private PadPipelineTechInfo createFullEntity() {
    var entity = new PadPipelineTechInfo();
    entity.setEstimatedAssetLife(5);
    entity.setPipelineDesignedToStandards(true);
    entity.setPipelineStandardsDescription("description");
    entity.setCorrosionDescription("description");
    entity.setPlannedPipelineTieInPoints(true);
    entity.setTieInPointsDescription("description");
    return entity;
  }

  @Test
  void mapEntityToForm_full() {
    var actualForm = new PipelineTechInfoForm();
    var entity = createFullEntity();
    pipelineTechInfoMappingService.mapEntityToForm(actualForm, entity);
    assertThat(actualForm).isEqualTo(createFullForm());
  }

  @Test
  void mapEntityToForm_partial() {
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
  void mapFormToEntity_full() {
    var actualEntity = new PadPipelineTechInfo();
    var form = createFullForm();
    pipelineTechInfoMappingService.mapFormToEntity(form, actualEntity);
    assertThat(actualEntity).isEqualTo(createFullEntity());
  }

  @Test
  void mapFormToEntity_partial() {
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
