package uk.co.ogauthority.pwa.features.termsandconditions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsPwaView;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsVariation;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsVariationForm;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsPwaViewRepository;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsVariationRepository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsVariationServiceTest {

  @Mock
  TermsAndConditionsVariationRepository termsAndConditionsVariationRepository;

  @Mock
  TermsAndConditionsVariationValidator termsAndConditionsVariationValidator;

  @Mock
  MasterPwaService masterPwaService;

  @Mock
  TermsAndConditionsPwaViewRepository termsAndConditionsPwaViewRepository;

  private TermsAndConditionsVariationService termsAndConditionsVariationService;

  private MasterPwa masterPwa;



  @Before
  public void setup() {
    termsAndConditionsVariationService = new TermsAndConditionsVariationService(
        termsAndConditionsVariationRepository,
        termsAndConditionsVariationValidator,
        masterPwaService,
        termsAndConditionsPwaViewRepository
    );
        masterPwa = MasterPwaTestUtil.create(1);
  }

  @Test
  public void saveForm() {
    var instant = Instant.parse("2023-06-30T15:43:00Z");
    try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
      mockedStatic.when(Instant::now).thenReturn(instant);

      var variationToBeSaved = new TermsAndConditionsVariation()
          .setMasterPwa(masterPwa)
          .setVariationTerm(7)
          .setHuooTerms("3, 6 & 9")
          .setDepconParagraph(2)
          .setDepconSchedule(8)
          .setCreatedBy(1)
          .setCreatedTimestamp(instant);

      var variationForm = new TermsAndConditionsVariationForm()
          .setPwaId(1)
          .setVariationTerm(7)
          .setHuooTermOne(3)
          .setHuooTermTwo(9)
          .setHuooTermThree(6)
          .setDepconParagraph(2)
          .setDepconSchedule(8);

      when(masterPwaService.getMasterPwaById(1)).thenReturn(masterPwa);

      termsAndConditionsVariationService.saveForm(variationForm, 1);

      verify(termsAndConditionsVariationRepository).save(refEq(variationToBeSaved));
    }
  }

  @Test
  public void getPwasForSelector() {
    when(termsAndConditionsPwaViewRepository.findAll()).thenReturn(
        List.of(new TermsAndConditionsPwaView(1, "1/W/23")));
    assertThat(termsAndConditionsVariationService.getPwasForSelector()).containsExactly(entry("1","1/W/23"));
  }

}