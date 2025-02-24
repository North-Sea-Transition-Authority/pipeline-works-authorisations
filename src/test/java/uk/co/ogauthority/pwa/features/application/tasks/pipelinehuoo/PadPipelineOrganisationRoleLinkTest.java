package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrgRoleInstanceType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

@ExtendWith(MockitoExtension.class)
class PadPipelineOrganisationRoleLinkTest {
  private final int PIPELINE_ID = 1;
  private final String FROM_LOCATION = "FROM";
  private final String TO_LOCATION = "TO";
  private final int SECTION_NUMBER = 1;

  private PadPipelineOrganisationRoleLink padPipelineOrganisationRoleLink;

  @BeforeEach
  void setup() {
    var pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);
    padPipelineOrganisationRoleLink = new PadPipelineOrganisationRoleLink();
    padPipelineOrganisationRoleLink.setPipeline(pipeline);
  }


  @Test
  void getOrgRoleInstanceType_whenNoSplitDetailsSet() {
    assertThat(padPipelineOrganisationRoleLink.getOrgRoleInstanceType()).isEqualTo(OrgRoleInstanceType.FULL_PIPELINE);
  }

  @Test
  void getOrgRoleInstanceType_whenSplitDetailsSet() {
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setSectionNumber(SECTION_NUMBER);

    assertThat(padPipelineOrganisationRoleLink.getOrgRoleInstanceType()).isEqualTo(OrgRoleInstanceType.SPLIT_PIPELINE);
  }

  @Test
  void getPipelineIdentifier_whenSplitDetailsSet() {
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);
    padPipelineOrganisationRoleLink.setSectionNumber(SECTION_NUMBER);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo(
        PipelineSection.from(
            new PipelineId(PIPELINE_ID),
            SECTION_NUMBER,
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        )
    );
  }


  @Test
  void visit_whenPipelineId(){
    padPipelineOrganisationRoleLink.setFromLocation(FROM_LOCATION);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    padPipelineOrganisationRoleLink.setToLocation(TO_LOCATION);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);
    padPipelineOrganisationRoleLink.setSectionNumber(SECTION_NUMBER);

    var newPipelineId = new PipelineId(2);
    newPipelineId.accept(padPipelineOrganisationRoleLink);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo(newPipelineId);
    assertThat(padPipelineOrganisationRoleLink.getFromLocation()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getFromLocationIdentInclusionMode()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getToLocation()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getToLocationIdentInclusionMode()).isNull();
    assertThat(padPipelineOrganisationRoleLink.getSectionNumber()).isNull();
  }

  @Test
  void visit_whenPipelineSection(){
    padPipelineOrganisationRoleLink.setFromLocation(null);
    padPipelineOrganisationRoleLink.setFromLocationIdentInclusionMode(null);
    padPipelineOrganisationRoleLink.setToLocation(null);
    padPipelineOrganisationRoleLink.setToLocationIdentInclusionMode(null);
    padPipelineOrganisationRoleLink.setSectionNumber(null);

    var newPipelineSection = PipelineSection.from(
        new PipelineId(2),
        SECTION_NUMBER,
        PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
        PipelineIdentPoint.exclusivePoint(TO_LOCATION)
    );
    newPipelineSection.accept(padPipelineOrganisationRoleLink);

    assertThat(padPipelineOrganisationRoleLink.getPipelineIdentifier()).isEqualTo( newPipelineSection);
    assertThat(padPipelineOrganisationRoleLink.getFromLocation()).isEqualTo(FROM_LOCATION);
    assertThat(padPipelineOrganisationRoleLink.getFromLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.INCLUSIVE);
    assertThat(padPipelineOrganisationRoleLink.getToLocation()).isEqualTo(TO_LOCATION);
    assertThat(padPipelineOrganisationRoleLink.getToLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.EXCLUSIVE);
    assertThat(padPipelineOrganisationRoleLink.getSectionNumber()).isEqualTo(SECTION_NUMBER);
  }
}