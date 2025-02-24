package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationSubmissionController;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmissionUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationViewServiceTest {

  private AsBuiltNotificationViewService asBuiltNotificationViewService;

  @Mock
  private PersonService personService;

  private final Person person = PersonTestUtil.createDefaultPerson();

  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(10, new PipelineId(20), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
      .createDefaultAsBuiltNotificationGroupPipeline(pipelineDetail.getPipelineDetailId());
  private final AsBuiltNotificationSubmission asBuiltNotificationSubmission = AsBuiltNotificationSubmissionUtil
      .createAsBuiltNotificationSubmission_withPerson_withStatus(asBuiltNotificationGroupPipeline, person,
          AsBuiltNotificationStatus.PER_CONSENT);

  @BeforeEach
  void setup() {
    asBuiltNotificationViewService = new AsBuiltNotificationViewService(personService);
  }

  @Test
  void mapToAsBuiltNotificationView_withExistingSubmission() {

    when(personService.getPersonById(any(PersonId.class))).thenReturn(person);

    assertThat(asBuiltNotificationViewService.mapToAsBuiltNotificationView(pipelineDetail, asBuiltNotificationSubmission))
    .extracting(
        AsBuiltNotificationView::getAsBuiltGroupReference,
        AsBuiltNotificationView::getPipelineNumber,
        AsBuiltNotificationView::getPipelineTypeDisplay,
        AsBuiltNotificationView::getSubmittedByPersonName,
        AsBuiltNotificationView::getSubmittedOnInstant,
        AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay,
        AsBuiltNotificationView::getDateWorkCompleted,
        AsBuiltNotificationView::getDateBroughtIntoUse,
        AsBuiltNotificationView::getOgaSubmissionReason,
        AsBuiltNotificationView::getSubmissionLink
        )
        .containsExactly(
            asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getReference(),
            pipelineDetail.getPipelineNumber(),
            pipelineDetail.getPipelineType().getDisplayName(),
            person.getFullName(),
            asBuiltNotificationSubmission.getSubmittedTimestamp(),
            asBuiltNotificationSubmission.getAsBuiltNotificationStatus().getDisplayName(),
            asBuiltNotificationSubmission.getDateWorkCompleted(),
            asBuiltNotificationSubmission.getDatePipelineBroughtIntoUse(),
            asBuiltNotificationSubmission.getRegulatorSubmissionReason(),
            ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
                .renderSubmitAsBuiltNotificationForm(
                    asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getId(),
                    pipelineDetail.getPipelineDetailId().asInt(), null, null))
    );
  }

  @Test
  void mapToAsBuiltNotificationView_noExistingSubmission() {
    assertThat(asBuiltNotificationViewService
        .mapToAsBuiltNotificationViewWithNoSubmission(asBuiltNotificationGroupPipeline.getAsBuiltNotificationGroup().getId(),
            pipelineDetail))
        .extracting(
            AsBuiltNotificationView::getAsBuiltGroupReference,
            AsBuiltNotificationView::getPipelineNumber,
            AsBuiltNotificationView::getPipelineTypeDisplay,
            AsBuiltNotificationView::getSubmittedByPersonName,
            AsBuiltNotificationView::getSubmittedOnInstant,
            AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay,
            AsBuiltNotificationView::getDateWorkCompleted,
            AsBuiltNotificationView::getDateBroughtIntoUse,
            AsBuiltNotificationView::getOgaSubmissionReason,
            AsBuiltNotificationView::getSubmissionLink)
        .containsExactly(
            null,
            pipelineDetail.getPipelineNumber(),
            pipelineDetail.getPipelineType().getDisplayName(),
            null,
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

  @Test
  void getSubmissionHistoryView_noPreviousHistory() {
    assertThat(asBuiltNotificationViewService.getSubmissionHistoryView(List.of()))
        .extracting(AsBuiltSubmissionHistoryView::getLatestSubmissionView, AsBuiltSubmissionHistoryView::getHistoricalSubmissionViews)
    .containsExactly(null, List.of());
  }

  @Test
  void getSubmissionHistoryView_onlyOneHistory() {

    when(personService.getPersonById(any(PersonId.class))).thenReturn(person);

    var asBuiltNotificationView = AsBuiltNotificationViewUtil
        .createHistoricAsBuiltNotificationView(asBuiltNotificationSubmission, person);
    assertThat(asBuiltNotificationViewService.getSubmissionHistoryView(List.of(asBuiltNotificationSubmission)))
        .extracting(
            view -> view.getLatestSubmissionView().getAsBuiltGroupReference(),
            view -> view.getLatestSubmissionView().getAsBuiltNotificationStatusDisplay(),
            AsBuiltSubmissionHistoryView::getHistoricalSubmissionViews
        )
        .containsExactly(asBuiltNotificationView.getAsBuiltGroupReference(), asBuiltNotificationView.getAsBuiltNotificationStatusDisplay(),
            List.of());
  }

  @Test
  void getSubmissionHistoryView_latestAndPreviousHistory() {

    when(personService.getPersonById(any(PersonId.class))).thenReturn(person);

    var olderSubmission = AsBuiltNotificationSubmissionUtil
        .createDefaultAsBuiltNotificationSubmission_fromStatusAndDatetime(AsBuiltNotificationStatus.NOT_PER_CONSENT,
            Instant.now().minusSeconds(100L));
    var asBuiltNotificationView1 = AsBuiltNotificationViewUtil
        .createHistoricAsBuiltNotificationView(asBuiltNotificationSubmission, person);
    var asBuiltNotificationView2 = AsBuiltNotificationViewUtil
        .createHistoricAsBuiltNotificationView(olderSubmission, person);
    var history = asBuiltNotificationViewService.getSubmissionHistoryView(List.of(asBuiltNotificationSubmission,
        olderSubmission));
    assertThat(history.getLatestSubmissionView())
        .extracting(
            AsBuiltNotificationView::getSubmittedOnInstant,
            AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay
        )
        .containsExactly(
            asBuiltNotificationView1.getSubmittedOnInstant(),
            asBuiltNotificationView1.getAsBuiltNotificationStatusDisplay());
    assertThat(history.getHistoricalSubmissionViews().get(0))
        .extracting(
            AsBuiltNotificationView::getAsBuiltNotificationStatusDisplay,
            AsBuiltNotificationView::getSubmittedOnInstant)
        .containsExactly(
            asBuiltNotificationView2.getAsBuiltNotificationStatusDisplay(),
            asBuiltNotificationView2.getSubmittedOnInstant());
  }

}
