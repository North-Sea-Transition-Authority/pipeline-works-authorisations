package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/* interface test for default methods */
@RunWith(MockitoJUnitRunner.class)
public class NamedPipelineTest {

  private final String PIPELINE_NUMBER = "PL123";
  private final String BUNDLE_NAME = "BUNDLE";
  private final BigDecimal MAX_EXTERNAL_DIAMETER = BigDecimal.TEN;

  private NamedPipelineTestImpl namedPipeline;

  @Before
  public void setUp() throws Exception {
    namedPipeline = new NamedPipelineTestImpl();
    namedPipeline.setPipelineNumber(PIPELINE_NUMBER);
  }

  @Test
  public void getPipelineName_whenOnlyPipelineNumberHasValue() {

    assertThat(namedPipeline.getPipelineName()).isEqualTo(PIPELINE_NUMBER + " - " + PipelineType.UNKNOWN.getDisplayName());
  }

  @Test
  public void getPipelineName_whenPipelineNumberHasValue_andPipelineTypeHasValue() {

    namedPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    assertThat(namedPipeline.getPipelineName()).isEqualTo(PIPELINE_NUMBER + " - " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName());
  }

  @Test
  public void getPipelineName_whenPipelineTypeIsSingleCore_andMaxExternalDiameterHasValue() {

    namedPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    namedPipeline.setMaxExternalDiameter(MAX_EXTERNAL_DIAMETER);
    assertThat(namedPipeline.getPipelineName())
        .isEqualTo(PIPELINE_NUMBER + " - 10 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName());
  }

  @Test
  public void getPipelineName_whenPipelineTypeIsSingleCore_andMaxExternalDiameterHasValue_andWithinBundle() {

    namedPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    namedPipeline.setMaxExternalDiameter(MAX_EXTERNAL_DIAMETER);
    namedPipeline.setPipelineInBundle(true);
    namedPipeline.setBundleName(BUNDLE_NAME);
    assertThat(namedPipeline.getPipelineName())
        .isEqualTo(PIPELINE_NUMBER + " - 10 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName() +
            String.format(" (%s)", BUNDLE_NAME));
  }

  @Test
  public void getPipelineName_whenPipelineTypeIsSingleCore_andMaxExternalDiameterHasValue_andWithinBundle_andNoBundleName() {

    namedPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    namedPipeline.setMaxExternalDiameter(MAX_EXTERNAL_DIAMETER);
    namedPipeline.setPipelineInBundle(true);
    assertThat(namedPipeline.getPipelineName())
        // included to complete coverage of actual behaviour. Probably not desired behaviour.
        .isEqualTo(PIPELINE_NUMBER + " - 10 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName() + " (null)");
  }

  /* create an implementation of the interface with the most basic implementation details to test default methods */
  public class NamedPipelineTestImpl implements NamedPipeline {

    private Integer pipelineId;
    private PipelineType pipelineType;
    private Boolean pipelineInBundle;
    private String bundleName;
    private BigDecimal maxExternalDiameter;
    private String pipelineNumber;

    @Override
    public Integer getPipelineId() {
      return this.pipelineId;
    }

    @Override
    public PipelineType getPipelineType() {
      return this.pipelineType;
    }

    @Override
    public Boolean getPipelineInBundle() {
      return this.pipelineInBundle;
    }

    @Override
    public String getBundleName() {
      return this.bundleName;
    }

    @Override
    public BigDecimal getMaxExternalDiameter() {
      return this.maxExternalDiameter;
    }

    @Override
    public String getPipelineNumber() {
      return this.pipelineNumber;
    }

    public void setPipelineId(Integer pipelineId) {
      this.pipelineId = pipelineId;
    }

    public void setPipelineType(PipelineType pipelineType) {
      this.pipelineType = pipelineType;
    }

    public void setPipelineInBundle(Boolean pipelineInBundle) {
      this.pipelineInBundle = pipelineInBundle;
    }

    public void setBundleName(String bundleName) {
      this.bundleName = bundleName;
    }

    public void setMaxExternalDiameter(BigDecimal maxExternalDiameter) {
      this.maxExternalDiameter = maxExternalDiameter;
    }

    public void setPipelineNumber(String pipelineNumber) {
      this.pipelineNumber = pipelineNumber;
    }
  }
}