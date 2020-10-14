package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.pipelinehuoo.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineOrganisationRoleLinkTest {
  private final int PIPELINE_ID = 1;
  private final String FROM_LOCATION = "FROM";
  private final String TO_LOCATION = "TO";

  private PadPipelineOrganisationRoleLink padPipelineOrganisationRoleLink;

  @Before
  public void setup() {
    var pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);
    padPipelineOrganisationRoleLink = new PadPipelineOrganisationRoleLink();
    padPipelineOrganisationRoleLink.setPipeline(pipeline);
  }


  @Test
  public void getOrgRoleInstanceType_whenNoSplitDetailsSet() {
    assertThat(padPipelineOrganisationRoleLink.getOrgRoleInstanceType()).isEqualTo(OrgRoleInstanceType.FULL_PIPELINE);
  }

  @Test
  public void getOrgRoleInstanceType_whenSplitDetailsSet() {
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);

    assertThat(padPipelineOrganisationRoleLink.getOrgRoleInstanceType()).isEqualTo(OrgRoleInstanceType.SPLIT_PIPELINE);
  }

  @Test
  public void getPipelineIdentifier_whenSplitDetailsSet() {
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo(
        PipelineSection.from(
            new PipelineId(PIPELINE_ID),
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        )
    );
  }


  @Test
  public void visit_whenPipelineId(){
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);

    var newPipelineId = new PipelineId(2);
    newPipelineId.accept(padPipelineOrganisationRoleLink);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo(newPipelineId);
    assertThat(padPipelineOrganisationRoleLink.getFromLocation()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getFromLocationIdentInclusionMode()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getToLocation()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getToLocationIdentInclusionMode()).isNull();
  }

  @Test
  public void visit_whenPipelineSection(){
    padPipelineOrganisationRoleLink.setFromLocation(null);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(null);
    padPipelineOrganisationRoleLink.setToLocation(null);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(null);

    var newPipelineSection = PipelineSection.from(
        new PipelineId(2),
        PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
        PipelineIdentPoint.exclusivePoint(TO_LOCATION)
    );
    newPipelineSection.accept(padPipelineOrganisationRoleLink);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo( newPipelineSection);
    assertThat(padPipelineOrganisationRoleLink.getFromLocation()).isEqualTo(FROM_LOCATION);
    assertThat(padPipelineOrganisationRoleLink.getFromLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.INCLUSIVE);
    assertThat(padPipelineOrganisationRoleLink.getToLocation()).isEqualTo(TO_LOCATION);
    assertThat(padPipelineOrganisationRoleLink.getToLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.EXCLUSIVE);
  }
}