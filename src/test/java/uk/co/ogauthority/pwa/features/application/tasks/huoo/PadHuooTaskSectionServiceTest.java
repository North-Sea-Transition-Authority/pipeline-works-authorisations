package uk.co.ogauthority.pwa.features.application.tasks.huoo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadHuooTaskSectionServiceTest {

  private static final EnumSet<PwaApplicationType> DEFAULT_CANNOT_SHOW_TASKS = EnumSet.of(
      PwaApplicationType.OPTIONS_VARIATION, PwaApplicationType.DEPOSIT_CONSENT
  );

  @Mock
  private PadOrganisationRolesRepository padOrganisationRolesRepository;

  @Mock
  private PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  @Mock
  private PadHuooValidationService padHuooValidationService;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PwaApplicationDetail detail;

  private PadHuooTaskSectionService padHuooTaskSectionService;

  @BeforeEach
  void setUp() throws Exception {

    padHuooTaskSectionService = new PadHuooTaskSectionService(
        padOrganisationRolesRepository,
        padPipelineOrganisationRoleLinkRepository,
        padOptionConfirmedService,
        padHuooValidationService,
        entityCopyingService
    );

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void canShowInTaskList_allowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    detail.setPwaApplication(app);

    PwaApplicationType.stream()
        .filter(pwaApplicationType -> !DEFAULT_CANNOT_SHOW_TASKS.contains(pwaApplicationType))
        .forEach(applicationType -> {

          app.setApplicationType(applicationType);

          assertThat(padHuooTaskSectionService.canShowInTaskList(detail)).isTrue();

        });

  }

  @Test
  void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    PwaApplicationType.stream()
        .filter(pwaApplicationType -> DEFAULT_CANNOT_SHOW_TASKS.contains(pwaApplicationType))
        .forEach(applicationType -> {

          app.setApplicationType(applicationType);

          assertThat(padHuooTaskSectionService.canShowInTaskList(detail)).isFalse();

        });
  }

  @Test
  void allowCopyOfSectionInformation_whenOptionsApp(){
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(padHuooTaskSectionService.allowCopyOfSectionInformation(detail)).isTrue();
  }

  @Test
  void allowCopyOfSectionInformation_whenDepositApp(){

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);

    assertThat(padHuooTaskSectionService.allowCopyOfSectionInformation(detail)).isFalse();
  }

  @Test
  void allowCopyOfSectionInformation_whenAppTypeNotHidden(){

    EnumSet.complementOf(DEFAULT_CANNOT_SHOW_TASKS).forEach(
        pwaApplicationType -> {
          var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(pwaApplicationType);
          assertThat(padHuooTaskSectionService.allowCopyOfSectionInformation(detail)).isTrue();
        }
    );
  }

  @Test
  void isComplete_validationResultIsValid() {

    var validationResult = HuooSummaryValidationResultTestUtil.validResult();
    when(padHuooValidationService.getHuooSummaryValidationResult(detail)).thenReturn(validationResult);

    var result = padHuooTaskSectionService.isComplete(detail);
    assertThat(result).isTrue();
  }

  @Test
  void isComplete_validationResultIsInvalid() {

    var validationResult = HuooSummaryValidationResultTestUtil.invalidResult();
    when(padHuooValidationService.getHuooSummaryValidationResult(detail)).thenReturn(validationResult);

    var result = padHuooTaskSectionService.isComplete(detail);
    assertThat(result).isFalse();
  }

}