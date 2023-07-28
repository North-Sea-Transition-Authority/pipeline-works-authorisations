package uk.co.ogauthority.pwa.features.termsandconditions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import uk.co.ogauthority.pwa.features.termsandconditions.model.PwaTermsAndConditions;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsManagementViewItem;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsPwaView;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsPwaViewRepository;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsServiceTest {

  @Mock
  TermsAndConditionsRepository termsAndConditionsRepository;

  @Mock
  TermsAndConditionsValidator termsAndConditionsValidator;

  @Mock
  MasterPwaService masterPwaService;

  @Mock
  TermsAndConditionsPwaViewRepository termsAndConditionsPwaViewRepository;

  private TermsAndConditionsService termsAndConditionsService;

  private MasterPwa masterPwa;

  private MasterPwaDetail detail;

  @Before
  public void setup() {
    termsAndConditionsService = new TermsAndConditionsService(
        termsAndConditionsRepository,
        termsAndConditionsValidator,
        masterPwaService,
        termsAndConditionsPwaViewRepository
    );

    masterPwa = MasterPwaTestUtil.create(1);
    detail = new MasterPwaDetail();
    detail.setMasterPwa(masterPwa);
    detail.setReference("1/W/23");
  }

  @Test
  public void saveForm() {
    var instant = Instant.parse("2023-06-30T15:43:00Z");
    try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);

      var variationToBeSaved = new PwaTermsAndConditions()
          .setMasterPwa(masterPwa)
          .setVariationTerm(7)
          .setHuooTermOne(3)
          .setHuooTermTwo(6)
          .setHuooTermThree(9)
          .setDepconParagraph(2)
          .setDepconSchedule(8)
          .setCreatedBy(new PersonId(1))
          .setCreatedTimestamp(instant);

      var variationForm = new TermsAndConditionsForm()
          .setPwaId(1)
          .setVariationTerm(7)
          .setHuooTermOne(3)
          .setHuooTermTwo(6)
          .setHuooTermThree(9)
          .setDepconParagraph(2)
          .setDepconSchedule(8);

      when(masterPwaService.getMasterPwaById(1)).thenReturn(masterPwa);

      var person = new Person(
          1,
          "Test",
          "Test",
          null,
          null
      );
      termsAndConditionsService.saveForm(variationForm, person);

      verify(termsAndConditionsRepository).save(refEq(variationToBeSaved));
    }
  }

  @Test
  public void getPwasForSelector() {
    when(termsAndConditionsPwaViewRepository.findAll()).thenReturn(
        List.of(new TermsAndConditionsPwaView(1, "1/W/23")));
    assertThat(termsAndConditionsService.getPwasForSelector()).containsExactly(entry("1","1/W/23"));
  }

  @Test
  public void getPwaManagementScreenPageView() {
    var terms = new PwaTermsAndConditions()
        .setMasterPwa(masterPwa)
        .setVariationTerm(7)
        .setHuooTermOne(3)
        .setHuooTermTwo(6)
        .setHuooTermThree(9)
        .setDepconSchedule(2)
        .setDepconParagraph(8)
        .setCreatedTimestamp(Instant.now());

    Page<PwaTermsAndConditions> page = new PageImpl<>(List.of(terms));

    var expectedPageView = new PageView<>(
        0,
        1,
        List.of(new TermsAndConditionsManagementViewItem(terms, "1/W/23")),
        null,
        1,
        0);

    when(masterPwaService.searchConsentedDetailsByReference("1/W/23")).thenReturn(List.of(detail));
    when(termsAndConditionsRepository.findAllByMasterPwaIn(any(), any())).thenReturn(page);

    assertThat(termsAndConditionsService.getPwaManagementScreenPageView(0, "1/W/23"))
        .usingRecursiveComparison().ignoringFields("pageUriFunction").isEqualTo(expectedPageView);
  }

  @Test
  public void findByMasterPwa() {
    var masterPwa = new MasterPwa();
    termsAndConditionsService.findByMasterPwa(masterPwa);
    verify(termsAndConditionsRepository).findPwaTermsAndConditionsByMasterPwa(masterPwa);
  }

}