package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.CONSENTED_PIPELINE_IMPORTED;
import static uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.NO_CONSENTED_PIPELINE_IMPORTED;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAndIdentViewFactoryTest {

  private static final PipelineId PIPELINE_ID = new PipelineId(100);

  private static final PipelineId CONSENTED_PIPELINE_ID = new PipelineId(1);
  private static final String CONSENTED_PIPELINE_NUMBER = "CONSENTED_PIPELINE";
  private static final PipelineId APPLICATION_NEW_PIPELINE_ID = new PipelineId(2);
  private static final String APPLICATION_NEW_PIPELINE_NUMBER = "NEW_PIPELINE";

  private static final PipelineType CONSENTED_PIPELINE_TYPE = PipelineType.PRODUCTION_FLOWLINE;
  private static final PipelineType APPLICATION_NEW_PIPELINE_TYPE = PipelineType.GAS_LIFT_JUMPER;

  private static final PipelineType IMPORTED_CONSENTED_PIPELINE_TYPE = PipelineType.CONTROL_JUMPER;

  private static final String POINT_A = "POINT A";
  private static final String POINT_B = "POINT B";
  private static final String POINT_C = "POINT C";
  private static final String POINT_D = "POINT D";

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PipelineDetailIdentService pipelineDetailIdentService;

  @Mock
  private PipelineOverview consentedPipelineOverview;

  @Mock
  private PipelineOverview applicationNewPipelineSummary;

  @Mock
  private PipelineOverview importedConsentedPipelineSummary;

  @Mock
  private IdentView ident1View;

  @Mock
  private IdentView ident2View;

  @Mock
  private IdentView ident3View;

  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  private PwaApplicationDetail detail;

  private PadPipeline padPipeline;

  @Before
  public void setUp() throws Exception {
    pipelineAndIdentViewFactory = new PipelineAndIdentViewFactory(
        padPipelineService,
        padPipelineIdentService,
        pipelineDetailService,
        pipelineDetailIdentService
    );

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    padPipeline = new PadPipeline(detail);

    when(ident1View.getIdentNumber()).thenReturn(1);
    when(ident2View.getIdentNumber()).thenReturn(2);
    when(ident3View.getIdentNumber()).thenReturn(3);
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentLocations_whenBothAppAndConsentedModelHavePipelineData_andNoAppIdentsExist() {
    // Make sure that if a pipeline is imported into app, but has all idents removed, we still return app version of pipeline.
    setupConsentedPipeline();
    setupApplicationPipelines(CONSENTED_PIPELINE_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var importedConsentedPipelineIdentViews = List.of(ident1View, ident3View);
    List<IdentView> applicationNewPipelineIdentViews = Collections.emptyList();
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(CONSENTED_PIPELINE_ID, importedConsentedPipelineIdentViews),
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, List.of(ident1View, ident2View, ident3View))
    );
    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(detail).stream()
        .sorted(
            Comparator.comparing(pipelineAndIdentViews -> pipelineAndIdentViews.getPipelineId().getPipelineIdAsInt()))
        .collect(Collectors.toList());


    assertThat(sortedResult).hasSize(2);
    assertThat(sortedResult.get(0).getPipelineId()).isEqualTo(CONSENTED_PIPELINE_ID);
    assertThat(sortedResult.get(0).getPipelineOverview()).isEqualTo(importedConsentedPipelineSummary);
    assertThat(sortedResult.get(0).getSortedIdentViews()).isEmpty();

    assertThat(sortedResult.get(1).getPipelineId()).isEqualTo(APPLICATION_NEW_PIPELINE_ID);
    assertThat(sortedResult.get(1).getPipelineOverview()).isEqualTo(applicationNewPipelineSummary);
    assertThat(sortedResult.get(1).getSortedIdentViews()).isEqualTo(applicationNewPipelineIdentViews);
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentLocations_whenBothAppAndConsentedModelHavePipelineData() {

    setupConsentedPipeline();
    setupApplicationPipelines(CONSENTED_PIPELINE_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var importedConsentedPipelineIdentViews = List.of(ident1View, ident3View);
    var applicationNewPipelineIdentViews = List.of(ident1View);
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(CONSENTED_PIPELINE_ID, importedConsentedPipelineIdentViews),
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, List.of(ident1View, ident2View, ident3View))
    );
    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(detail).stream()
        .sorted(
            Comparator.comparing(pipelineAndIdentViews -> pipelineAndIdentViews.getPipelineId().getPipelineIdAsInt()))
        .collect(Collectors.toList());


    assertThat(sortedResult).hasSize(2);
    assertThat(sortedResult.get(0).getPipelineId()).isEqualTo(CONSENTED_PIPELINE_ID);
    assertThat(sortedResult.get(0).getPipelineOverview()).isEqualTo(importedConsentedPipelineSummary);
    assertThat(sortedResult.get(0).getSortedIdentViews()).isEqualTo(importedConsentedPipelineIdentViews);

    assertThat(sortedResult.get(1).getPipelineId()).isEqualTo(APPLICATION_NEW_PIPELINE_ID);
    assertThat(sortedResult.get(1).getPipelineOverview()).isEqualTo(applicationNewPipelineSummary);
    assertThat(sortedResult.get(1).getSortedIdentViews()).isEqualTo(applicationNewPipelineIdentViews);
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentLocations_whenAppHasNoConsentedPipelineData() {

    setupConsentedPipeline();
    setupApplicationPipelines(NO_CONSENTED_PIPELINE_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var applicationNewPipelineIdentViews = List.of(ident1View);
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, List.of(ident1View, ident2View, ident3View))
    );
    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(detail).stream()
        .sorted(
            Comparator.comparing(pipelineAndIdentViews -> pipelineAndIdentViews.getPipelineId().getPipelineIdAsInt()))
        .collect(Collectors.toList());

    assertThat(sortedResult).hasSize(2);
    assertThat(sortedResult.get(0).getPipelineId()).isEqualTo(CONSENTED_PIPELINE_ID);
    assertThat(sortedResult.get(0).getPipelineOverview()).isEqualTo(consentedPipelineOverview);
    assertThat(sortedResult.get(0).getSortedIdentViews()).isEqualTo(consentedPipelineIdentViews);

    assertThat(sortedResult.get(1).getPipelineId()).isEqualTo(APPLICATION_NEW_PIPELINE_ID);
    assertThat(sortedResult.get(1).getPipelineOverview()).isEqualTo(applicationNewPipelineSummary);
    assertThat(sortedResult.get(1).getSortedIdentViews()).isEqualTo(applicationNewPipelineIdentViews);
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentLocations_whenOnlyConsentedPipeline() {

    setupConsentedPipeline();

    var allAppAndMasterPwaPipelineIds = Set.of(CONSENTED_PIPELINE_ID);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, consentedPipelineIdentViews)
    );
    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);

    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(detail).stream()
        .sorted(
            Comparator.comparing(pipelineAndIdentViews -> pipelineAndIdentViews.getPipelineId().getPipelineIdAsInt()))
        .collect(Collectors.toList());

    assertThat(sortedResult).hasSize(1);
    assertThat(sortedResult.get(0).getPipelineId()).isEqualTo(CONSENTED_PIPELINE_ID);
    assertThat(sortedResult.get(0).getPipelineOverview()).isEqualTo(consentedPipelineOverview);
    assertThat(sortedResult.get(0).getSortedIdentViews()).isEqualTo(consentedPipelineIdentViews);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_returnsApplicationVersionOfImportedPipeline() {
    setupApplicationPipelines(CONSENTED_PIPELINE_IMPORTED);
    setupConsentedPipeline();

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(detail);

    assertThat(allPipelinesMap).hasSize(2);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(CONSENTED_PIPELINE_ID).getPipelineType())
        .isEqualTo(IMPORTED_CONSENTED_PIPELINE_TYPE);

    assertThat(allPipelinesMap.get(APPLICATION_NEW_PIPELINE_ID).getPipelineType())
        .isEqualTo(APPLICATION_NEW_PIPELINE_TYPE);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_containsConsentedPipelineDetails_whenNoAppPipeline_andNoImportedPipeline() {

    setupConsentedPipeline();

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(detail);

    assertThat(allPipelinesMap).hasSize(1);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(CONSENTED_PIPELINE_ID).getPipelineType())
        .isEqualTo(CONSENTED_PIPELINE_TYPE);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_containsAppPipelineDetails_whenNoConsentedPipeline() {

    setupApplicationPipelines(NO_CONSENTED_PIPELINE_IMPORTED);

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(detail);

    assertThat(allPipelinesMap).hasSize(1);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(APPLICATION_NEW_PIPELINE_ID).getPipelineType())
        .isEqualTo(APPLICATION_NEW_PIPELINE_TYPE);

  }

  @Test
  public void getPipelineSortedIdentViews_whenApplicationPipelineFound() {
    when(padPipelineService.findByPwaApplicationDetailAndPipelineId(detail, PIPELINE_ID))
        .thenReturn(Optional.of(padPipeline));

    var identViewList = List.of(ident3View, ident2View, ident1View);

    when(padPipelineIdentService.getIdentViews(padPipeline))
        .thenReturn(identViewList);

    var idents = pipelineAndIdentViewFactory.getPipelineSortedIdentViews(detail, PIPELINE_ID);

    assertThat(idents).containsExactly(ident1View, ident2View, ident3View);

  }

  @Test
  public void getPipelineSortedIdentViews_whenConsentedPipelineOnlyFound() {
    when(padPipelineService.findByPwaApplicationDetailAndPipelineId(detail, PIPELINE_ID))
        .thenReturn(Optional.empty());

    var identViewList = List.of(ident1View, ident2View);

    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(PIPELINE_ID))
        .thenReturn(identViewList);

    var idents = pipelineAndIdentViewFactory.getPipelineSortedIdentViews(detail, PIPELINE_ID);

    assertThat(idents).containsExactly(ident1View, ident2View);

  }

  @Test
  public void getPipelineSortedIdentViews_whenPipelineNotFound() {

    var options = pipelineAndIdentViewFactory.getPipelineSortedIdentViews(detail, PIPELINE_ID);

    assertThat(options).isEmpty();
  }

  private void setupConsentedPipeline() {
    when(consentedPipelineOverview.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID.asInt());
    when(consentedPipelineOverview.getPipelineType()).thenReturn(CONSENTED_PIPELINE_TYPE);
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwa(detail.getPwaApplication().getMasterPwa()))
        .thenReturn(List.of(consentedPipelineOverview));

  }

  private void setupApplicationPipelines(ConsentedPipelineImportedIntoApplication consentedPipelineImportedIntoApplication) {
    when(applicationNewPipelineSummary.getPipelineId()).thenReturn(APPLICATION_NEW_PIPELINE_ID.asInt());
    when(applicationNewPipelineSummary.getPipelineType()).thenReturn(APPLICATION_NEW_PIPELINE_TYPE);

    // same pipeline as consented pipeline but within the application with a different type
    when(importedConsentedPipelineSummary.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID.asInt());
    when(importedConsentedPipelineSummary.getPipelineType()).thenReturn(IMPORTED_CONSENTED_PIPELINE_TYPE);

    if (consentedPipelineImportedIntoApplication.equals(CONSENTED_PIPELINE_IMPORTED)) {
      when(padPipelineService.getApplicationPipelineOverviews(detail))
          .thenReturn(List.of(importedConsentedPipelineSummary, applicationNewPipelineSummary));
    } else {
      when(padPipelineService.getApplicationPipelineOverviews(detail))
          .thenReturn(List.of(applicationNewPipelineSummary));
    }
  }

  enum ConsentedPipelineImportedIntoApplication {
    CONSENTED_PIPELINE_IMPORTED,
    NO_CONSENTED_PIPELINE_IMPORTED
  }

}