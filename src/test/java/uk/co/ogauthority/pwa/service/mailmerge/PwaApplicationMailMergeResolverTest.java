package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationMailMergeResolverTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  private PwaApplicationMailMergeResolver pwaApplicationMailMergeResolver;

  private List<MailMergeField> mailMergeFields;

  @Before
  public void setUp() throws Exception {

    pwaApplicationMailMergeResolver = new PwaApplicationMailMergeResolver(pwaApplicationDetailService, padProjectInformationService);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .map(v -> {
          var f = new MailMergeField();
          f.setMnem(v);
          return f;
        })
        .collect(Collectors.toList());

  }

  @Test
  public void supportsDocumentSource_app_true() {

    var app = new PwaApplication();

    boolean supported = pwaApplicationMailMergeResolver.supportsDocumentSource(app);

    assertThat(supported).isTrue();

  }

  @Test
  public void resolveMergeFields() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getLatestSubmittedDetail(detail.getPwaApplication()))
        .thenReturn(Optional.of(detail));

    var start = Instant.now();
    var projectInfo = new PadProjectInformation();
    projectInfo.setProjectName("proj");
    projectInfo.setProposedStartTimestamp(start);

    when(padProjectInformationService.getPadProjectInformationData(detail)).thenReturn(projectInfo);

    var fieldToValueMap = pwaApplicationMailMergeResolver
        .resolveMergeFields(detail.getPwaApplication(), mailMergeFields);

    assertThat(fieldToValueMap).containsExactlyInAnyOrderEntriesOf(Map.of(
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), DateUtils.formatDate(start),
        MailMergeFieldMnem.PROJECT_NAME.name(), projectInfo.getProjectName()
    ));

  }

}