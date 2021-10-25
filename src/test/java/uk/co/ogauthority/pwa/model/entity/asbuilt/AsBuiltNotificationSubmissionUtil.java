package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

public class AsBuiltNotificationSubmissionUtil {

  public static AsBuiltNotificationSubmission createDefaultAsBuiltNotificationSubmission_fromStatus(AsBuiltNotificationStatus asBuiltNotificationStatus) {
    var pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(210, new PipelineId(200), Instant.now());
    var person = PersonTestUtil.createDefaultPerson();
    var asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
        .createDefaultAsBuiltNotificationGroupPipeline(pipelineDetail.getPipelineDetailId());
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), Instant.now(),
        asBuiltNotificationStatus, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

  public static AsBuiltNotificationSubmission createDefaultAsBuiltNotificationSubmission_fromStatusAndDatetime(
      AsBuiltNotificationStatus asBuiltNotificationStatus, Instant instant) {
    var pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(210, new PipelineId(200), Instant.now());
    var person = PersonTestUtil.createDefaultPerson();
    var asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
        .createDefaultAsBuiltNotificationGroupPipeline(pipelineDetail.getPipelineDetailId());
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), instant,
        asBuiltNotificationStatus, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

  public static AsBuiltNotificationSubmission createDefaultAsBuiltNotificationSubmission_fromPipelineDetail(
      PipelineDetail pipelineDetail, AsBuiltNotificationStatus asBuiltNotificationStatus) {
    var person = PersonTestUtil.createDefaultPerson();
    var asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
        .createDefaultAsBuiltNotificationGroupPipeline(pipelineDetail.getPipelineDetailId());
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), Instant.now(),
        asBuiltNotificationStatus, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

  public static AsBuiltNotificationSubmission createAsBuiltNotificationSubmission_withPerson(AsBuiltNotificationGroupPipeline
                                                                                             asBuiltNotificationGroupPipeline,
                                                                                             Person person) {
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), Instant.now(),
        AsBuiltNotificationStatus.PER_CONSENT, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

  public static AsBuiltNotificationSubmission createAsBuiltNotificationSubmission_withPersonAndSubmittedDateTime(AsBuiltNotificationGroupPipeline
                                                                                                 asBuiltNotificationGroupPipeline,
                                                                                             Person person, Instant instant) {
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), instant,
        AsBuiltNotificationStatus.PER_CONSENT, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

  public static AsBuiltNotificationSubmission createAsBuiltNotificationSubmission_withPerson_withStatus(AsBuiltNotificationGroupPipeline
                                                                                                            asBuiltNotificationGroupPipeline,
                                                                                                        Person person,
                                                                                                        AsBuiltNotificationStatus asBuiltNotificationStatus) {
    return new AsBuiltNotificationSubmission(100, asBuiltNotificationGroupPipeline, person.getId(), Instant.now(),
        asBuiltNotificationStatus, LocalDate.now(), LocalDate.now(), "Regulator submission reason", true);
  }

}