package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationSubmissionController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmissionUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationViewServiceTest {

  private AsBuiltNotificationViewService asBuiltNotificationViewService;

  @Mock
  private PersonService personService;

  private final Person person = PersonTestUtil.createDefaultPerson();

  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(10, new PipelineId(20), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
      .createDefaultAsBuiltNotificationGroupPipeline(pipelineDetail.getPipelineDetailId());
  private final AsBuiltNotificationSubmission asBuiltNotificationSubmission = AsBuiltNotificationSubmissionUtil
      .createAsBuiltNotificationSubmission_withPerso_withStatus(asBuiltNotificationGroupPipeline, person, AsBuiltNotificationStatus.PER_CONSENT);

  @Before
  public void setup() {
    asBuiltNotificationViewService = new AsBuiltNotificationViewService(personService);

    when(personService.getPersonById(any())).thenReturn(person);
  }

  @Test
  public void mapToAsBuiltNotificationView_withExistingSubmission() {
    assertThat(asBuiltNotificationViewService.mapToAsBuiltNotificationView(pipelineDetail, asBuiltNotificationSubmission))
    .extracting(AsBuiltNotificationView::getPipelineNumber,
        AsBuiltNotificationView::getPipelineTypeDisplay,
        AsBuiltNotificationView::getSubmittedByPersonName,
        AsBuiltNotificationView::getSubmittedOnInstant,
        AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay,
        AsBuiltNotificationView::getDateLaid,
        AsBuiltNotificationView::getDateBroughtIntoUse,
        AsBuiltNotificationView::getSubmissionLink)
        .containsExactly(
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        person.getFullName(),
        asBuiltNotificationSubmission.getSubmittedTimestamp(),
        asBuiltNotificationSubmission.getAsBuiltNotificationStatus().getDisplayName(),
        asBuiltNotificationSubmission.getDateLaid(),
        asBuiltNotificationSubmission.getDatePipelineBroughtIntoUse(),
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .renderSubmitAsBuiltNotificationForm(
                asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getId(),
                pipelineDetail.getPipelineDetailId().asInt(), null, null))
    );
  }

  @Test
  public void mapToAsBuiltNotificationView_noExistingSubmission() {
    assertThat(asBuiltNotificationViewService
        .mapToAsBuiltNotificationViewWithNoSubmission(asBuiltNotificationGroupPipeline.getAsBuiltNotificationGroup().getId(),
            pipelineDetail))
        .extracting(AsBuiltNotificationView::getPipelineNumber,
            AsBuiltNotificationView::getPipelineTypeDisplay,
            AsBuiltNotificationView::getSubmittedByPersonName,
            AsBuiltNotificationView::getSubmittedOnInstant,
            AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay,
            AsBuiltNotificationView::getDateLaid,
            AsBuiltNotificationView::getDateBroughtIntoUse,
            AsBuiltNotificationView::getSubmissionLink)
        .containsExactly(
            pipelineDetail.getPipelineNumber(),
            pipelineDetail.getPipelineType().getDisplayName(),
            null,
            null,
            null,
            null,
            null,
            ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
                .renderSubmitAsBuiltNotificationForm(
                    asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getId(),
                    pipelineDetail.getPipelineDetailId().asInt(), null, null))
        );
  }


}
