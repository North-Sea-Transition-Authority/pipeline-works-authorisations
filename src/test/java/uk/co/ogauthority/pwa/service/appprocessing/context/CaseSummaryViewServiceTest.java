package uk.co.ogauthority.pwa.service.appprocessing.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailViewRepository;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class CaseSummaryViewServiceTest {

  @Mock
  private ApplicationDetailViewRepository applicationDetailViewRepository;

  private CaseSummaryViewService caseSummaryViewService;

  private ApplicationDetailView applicationDetailView;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    caseSummaryViewService = new CaseSummaryViewService(applicationDetailViewRepository);

    detail = new PwaApplicationDetail();
    detail.setId(1);

    applicationDetailView = ApplicationDetailViewTestUtil.createGenericDetailView();

    when(applicationDetailViewRepository.findByPwaApplicationDetailId(anyInt())).thenReturn(Optional.of(applicationDetailView));

  }

  @Test
  public void getCaseSummaryViewForAppDetail_present() {

    var caseSummaryViewOpt = caseSummaryViewService.getCaseSummaryViewForAppDetail(detail);

    assertThat(caseSummaryViewOpt).isPresent();

    var caseSummaryView = caseSummaryViewOpt.get();

    var holderNameStringList = Arrays.stream(caseSummaryView.getHolderNames().split(", "))
        .collect(Collectors.toList());

    var fieldNameList = Arrays.stream(caseSummaryView.getFieldNames().split(", "))
        .collect(Collectors.toList());

    assertThat(caseSummaryView.getPwaApplicationRef()).isEqualTo(applicationDetailView.getPadReference());
    assertThat(caseSummaryView.getPwaApplicationTypeDisplay()).isEqualTo(applicationDetailView.getApplicationType().getDisplayName());
    assertThat(holderNameStringList).containsExactlyInAnyOrderElementsOf(applicationDetailView.getPadHolderNameList());
    assertThat(fieldNameList).containsExactlyInAnyOrderElementsOf(applicationDetailView.getPadFields());
    assertThat(caseSummaryView.getCaseOfficerName()).isEqualTo(applicationDetailView.getCaseOfficerName());
    assertThat(caseSummaryView.getProposedStartDateDisplay()).isEqualTo(DateUtils.formatDate(applicationDetailView.getPadProposedStart()));
    assertThat(caseSummaryView.isFastTrackFlag()).isEqualTo(applicationDetailView.isSubmittedAsFastTrackFlag());
    assertThat(caseSummaryView.getVersionNo()).isEqualTo(applicationDetailView.getVersionNo());

  }

  @Test
  public void getCaseSummaryViewForAppDetail_notPresent() {

    when(applicationDetailViewRepository.findByPwaApplicationDetailId(anyInt())).thenReturn(Optional.empty());

    var caseSummaryViewOpt = caseSummaryViewService.getCaseSummaryViewForAppDetail(detail);

    assertThat(caseSummaryViewOpt).isEmpty();

  }

}