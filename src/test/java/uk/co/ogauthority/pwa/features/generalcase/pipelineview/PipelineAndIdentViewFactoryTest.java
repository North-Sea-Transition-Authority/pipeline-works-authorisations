package uk.co.ogauthority.pwa.features.generalcase.pipelineview;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.CONSENTED_PIPELINE_EXISTS_AND_IMPORTED;
import static uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED;
import static uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.ONLY_APPLICATION_PIPELINE_EXISTS;
import static uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactoryTest.ConsentedPipelineImportedIntoApplication.ONLY_CONSENTED_PIPELINE_EXISTS;

import java.time.Clock;
import java.time.Instant;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentViewService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAndIdentViewFactoryTest {

  private static final PipelineId PIPELINE_ID = new PipelineId(100);

  private static final PipelineId CONSENTED_PIPELINE_ID = new PipelineId(1);
  private static final PipelineId APPLICATION_NEW_PIPELINE_ID = new PipelineId(2);

  private static final PipelineType CONSENTED_PIPELINE_TYPE = PipelineType.PRODUCTION_FLOWLINE;
  private static final PipelineType APPLICATION_NEW_PIPELINE_TYPE = PipelineType.GAS_LIFT_JUMPER;

  private static final PipelineType IMPORTED_CONSENTED_PIPELINE_TYPE = PipelineType.CONTROL_JUMPER;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PipelineDetailIdentViewService pipelineDetailIdentViewService;

  @Mock
  private PwaConsentService pwaConsentService;

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

  @Mock
  private Clock clock;

  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  private PwaApplicationDetail detail;

  private PadPipeline padPipeline;

  private final Instant clockTime = Instant.now();

  private PwaConsent pwaConsent;

  @Before
  public void setUp() throws Exception {

    when(clock.instant()).thenReturn(clockTime);

    pipelineAndIdentViewFactory = new PipelineAndIdentViewFactory(
        padPipelineService,
        padPipelineIdentService,
        pipelineDetailService,
        pipelineDetailIdentViewService,
        pwaConsentService,
        clock
    );

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    padPipeline = new PadPipeline(detail);

    when(ident1View.getIdentNumber()).thenReturn(1);
    when(ident2View.getIdentNumber()).thenReturn(2);
    when(ident3View.getIdentNumber()).thenReturn(3);

    pwaConsent = new PwaConsent();
    pwaConsent.setSourcePwaApplication(detail.getPwaApplication());
    pwaConsent.setConsentInstant(clockTime.minusSeconds(86400));

  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenBothAppAndConsentedModelHavePipelineData_andNoAppIdentsExist() {
    // Make sure that if a pipeline is imported into app, but has all idents removed, we still return app version of pipeline.
    setupPipelines(CONSENTED_PIPELINE_EXISTS_AND_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    List<IdentView> importedConsentedPipelineIdentViews = Collections.emptyList();
    List<IdentView> applicationNewPipelineIdentViews = Collections.emptyList();
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(CONSENTED_PIPELINE_ID, importedConsentedPipelineIdentViews),
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(detail, allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, consentedPipelineIdentViews)
    );
    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    )
        .stream()
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
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenBothAppAndConsentedModelHavePipelineData() {

    setupPipelines(CONSENTED_PIPELINE_EXISTS_AND_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var importedConsentedPipelineIdentViews = List.of(ident1View, ident3View);
    var applicationNewPipelineIdentViews = List.of(ident1View);
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(CONSENTED_PIPELINE_ID, importedConsentedPipelineIdentViews),
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(detail, allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, List.of(ident1View, ident2View, ident3View))
    );
    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    )
        .stream()
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
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenAppHasNoConsentedPipelineData() {

    setupPipelines(CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED);

    var allAppAndMasterPwaPipelineIds = Set.of(APPLICATION_NEW_PIPELINE_ID, CONSENTED_PIPELINE_ID);

    var applicationNewPipelineIdentViews = List.of(ident1View);
    var appPipelineIdentMap = Map.ofEntries(
        Map.entry(APPLICATION_NEW_PIPELINE_ID, applicationNewPipelineIdentViews)
    );

    when(padPipelineIdentService.getApplicationIdentViewsForPipelines(detail, allAppAndMasterPwaPipelineIds))
        .thenReturn(appPipelineIdentMap);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, List.of(ident1View, ident2View, ident3View))
    );
    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);


    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    )
        .stream()
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

  private void setupOnlyConsentedPipeline(){
    setupPipelines(ONLY_CONSENTED_PIPELINE_EXISTS);

    var allAppAndMasterPwaPipelineIds = Set.of(CONSENTED_PIPELINE_ID);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    var consentedPipelineIdentMap = Map.ofEntries(
        Map.entry(CONSENTED_PIPELINE_ID, consentedPipelineIdentViews)
    );
    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenOnlyConsentedPipeline() {

    setupPipelines(ONLY_CONSENTED_PIPELINE_EXISTS);

    var allAppAndMasterPwaPipelineIds = Set.of(CONSENTED_PIPELINE_ID);

    var consentedPipelineIdentViews = List.of(ident1View, ident2View, ident3View);
    var consentedPipelineIdentMap = Map.ofEntries(
        // pretend Ident has been removed within the app so theres a difference in the lists
        Map.entry(CONSENTED_PIPELINE_ID, consentedPipelineIdentViews)
    );
    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(allAppAndMasterPwaPipelineIds))
        .thenReturn(consentedPipelineIdentMap);

    var sortedResult = pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    )
        .stream()
        .sorted(
            Comparator.comparing(pipelineAndIdentViews -> pipelineAndIdentViews.getPipelineId().getPipelineIdAsInt()))
        .collect(Collectors.toList());

    assertThat(sortedResult).hasSize(1);
    assertThat(sortedResult.get(0).getPipelineId()).isEqualTo(CONSENTED_PIPELINE_ID);
    assertThat(sortedResult.get(0).getPipelineOverview()).isEqualTo(consentedPipelineOverview);
    assertThat(sortedResult.get(0).getSortedIdentViews()).isEqualTo(consentedPipelineIdentViews);

  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenConsentFilterIsAllPipelines() {

   setupOnlyConsentedPipeline();

    pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    );

    verify(pipelineDetailService)
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getMasterPwa(),
            PipelineStatus.currentStatusSet(),
            clock.instant()
        );
  }

  @Test
  public void getAllAppAndMasterPwaPipelineAndIdentViews_whenConsentFilterIsOnSeabedPipelines() {

    setupOnlyConsentedPipeline();

    pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    );

    verify(pipelineDetailService)
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getMasterPwa(),
            PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED),
            clock.instant()
        );
  }


  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_returnsApplicationVersionOfImportedPipeline() {
    setupPipelines(CONSENTED_PIPELINE_EXISTS_AND_IMPORTED);

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES);

    assertThat(allPipelinesMap).hasSize(2);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(CONSENTED_PIPELINE_ID).getPipelineType())
        .isEqualTo(IMPORTED_CONSENTED_PIPELINE_TYPE);

    assertThat(allPipelinesMap.get(APPLICATION_NEW_PIPELINE_ID).getPipelineType())
        .isEqualTo(APPLICATION_NEW_PIPELINE_TYPE);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_containsConsentedPipelineDetails_whenNoAppPipeline_andNoImportedPipeline() {

    setupPipelines(ONLY_CONSENTED_PIPELINE_EXISTS);

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES);

    assertThat(allPipelinesMap).hasSize(1);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(CONSENTED_PIPELINE_ID).getPipelineType())
        .isEqualTo(CONSENTED_PIPELINE_TYPE);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_containsAppPipelineDetails_whenNoConsentedPipeline() {

    setupPipelines(ONLY_APPLICATION_PIPELINE_EXISTS);

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        detail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES);

    assertThat(allPipelinesMap).hasSize(1);

    // check that the type is the changed pipeline type
    assertThat(allPipelinesMap.get(APPLICATION_NEW_PIPELINE_ID).getPipelineType())
        .isEqualTo(APPLICATION_NEW_PIPELINE_TYPE);

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_usesConsentTimestampForPwaPipelines_whenApplicationConsented() {

    setupPipelines(CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED);
    var consentedPipelineFilter = PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES;

    when(pwaConsentService.getConsentByPwaApplication(detail.getPwaApplication())).thenReturn(Optional.of(pwaConsent));

    pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(detail, consentedPipelineFilter);

    verify(pipelineDetailService).getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
        detail.getPwaApplication().getMasterPwa(),
        consentedPipelineFilter.getPipelineStatusSet(),
        pwaConsent.getConsentInstant()
    );

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwa_usesCurrentTimeForPwaPipelines_whenApplicationNotConsented() {

    setupPipelines(CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED);
    var consentedPipelineFilter = PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES;

    pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(detail, consentedPipelineFilter);

    verify(pipelineDetailService).getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
        detail.getPwaApplication().getMasterPwa(),
        consentedPipelineFilter.getPipelineStatusSet(),
        clock.instant()
    );

  }

  @Test
  public void getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds() {

    setupPipelines(CONSENTED_PIPELINE_EXISTS_AND_IMPORTED);

    var allPipelinesMap = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
        detail, List.of(new PipelineId(2)));

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

    when(pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(PIPELINE_ID))
        .thenReturn(identViewList);

    var idents = pipelineAndIdentViewFactory.getPipelineSortedIdentViews(detail, PIPELINE_ID);

    assertThat(idents).containsExactly(ident1View, ident2View);

  }

  @Test
  public void getPipelineSortedIdentViews_whenPipelineNotFound() {

    var options = pipelineAndIdentViewFactory.getPipelineSortedIdentViews(detail, PIPELINE_ID);

    assertThat(options).isEmpty();
  }

  private void setupPipelines(ConsentedPipelineImportedIntoApplication consentedPipelineImportedIntoApplication) {
    when(consentedPipelineOverview.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID.asInt());
    when(consentedPipelineOverview.getPipelineType()).thenReturn(CONSENTED_PIPELINE_TYPE);
    when(consentedPipelineOverview.getPadPipelineId()).thenReturn(null);

    when(applicationNewPipelineSummary.getPipelineId()).thenReturn(APPLICATION_NEW_PIPELINE_ID.asInt());
    when(applicationNewPipelineSummary.getPipelineType()).thenReturn(APPLICATION_NEW_PIPELINE_TYPE);
    when(applicationNewPipelineSummary.getPadPipelineId()).thenReturn(998);

    // same pipeline as consented pipeline but within the application with a different type
    when(importedConsentedPipelineSummary.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID.asInt());
    when(importedConsentedPipelineSummary.getPipelineType()).thenReturn(IMPORTED_CONSENTED_PIPELINE_TYPE);
    when(importedConsentedPipelineSummary.getPadPipelineId()).thenReturn(999);

    switch(consentedPipelineImportedIntoApplication) {
      case CONSENTED_PIPELINE_EXISTS_AND_IMPORTED:

        when(padPipelineService.getApplicationPipelineOverviews(detail))
            .thenReturn(List.of(importedConsentedPipelineSummary, applicationNewPipelineSummary));

        when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getPwaApplication().getMasterPwa(),
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES.getPipelineStatusSet(),
            clock.instant())
        ).thenReturn(List.of(consentedPipelineOverview));

        break;

      case CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED:

        when(padPipelineService.getApplicationPipelineOverviews(detail))
            .thenReturn(List.of(applicationNewPipelineSummary));

        when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getPwaApplication().getMasterPwa(),
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES.getPipelineStatusSet(),
            clock.instant())
        ).thenReturn(List.of(consentedPipelineOverview));

        break;

      case ONLY_APPLICATION_PIPELINE_EXISTS:

        when(padPipelineService.getApplicationPipelineOverviews(detail)).thenReturn(List.of(applicationNewPipelineSummary));

        when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getPwaApplication().getMasterPwa(),
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES.getPipelineStatusSet(),
            clock.instant())
        ).thenReturn(List.of());

        break;

      case ONLY_CONSENTED_PIPELINE_EXISTS:

        when(padPipelineService.getApplicationPipelineOverviews(detail)).thenReturn(List.of());

        when(pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            detail.getPwaApplication().getMasterPwa(),
            PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES.getPipelineStatusSet(),
            clock.instant())
        ).thenReturn(List.of(consentedPipelineOverview));

        break;
    }

  }

  enum ConsentedPipelineImportedIntoApplication {
    CONSENTED_PIPELINE_EXISTS_AND_IMPORTED,
    CONSENTED_PIPELINE_EXISTS_NOT_IMPORTED,
    ONLY_CONSENTED_PIPELINE_EXISTS,
    ONLY_APPLICATION_PIPELINE_EXISTS
  }

}