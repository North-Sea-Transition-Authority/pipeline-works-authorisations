package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectionView;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PadAreaServiceTest {
  private final int DEVUK_FIELD_ID = 1;
  private final String DEVUK_FIELD_NAME = "FIELD_NAME";

  @Mock
  private PadAreaRepository padAreaRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private DevukFieldService devukFieldService;

  @Mock
  private SearchSelectorService searchSelectorService;

  @Mock
  private PwaAreaFormValidator pwaAreaFormValidator;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Captor
  private ArgumentCaptor<List<PadLinkedArea>> padFieldsArgumentCaptor;

  private PadAreaService padAreaService;
  private PwaApplicationDetail pwaApplicationDetail;

  private PadLinkedArea existingField;
  private DevukField devukField;
  private String manuallyEnteredFieldName = "my entered field";

  @BeforeEach
  void setUp() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setResourceType(PwaResourceType.PETROLEUM);
    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    devukField = new DevukField(DEVUK_FIELD_ID, DEVUK_FIELD_NAME, 100);

    existingField = new PadLinkedArea();
    existingField.setPwaApplicationDetail(pwaApplicationDetail);
    existingField.setDevukField(devukField);
    existingField.setAreaType(LinkedAreaType.FIELD);

    when(searchSelectorService.removePrefix(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName)).thenReturn(manuallyEnteredFieldName);

    when(devukFieldService.findById(DEVUK_FIELD_ID)).thenReturn(devukField);

    when(padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField));

    padAreaService = new PadAreaService(
        padAreaRepository,
        pwaApplicationDetailService,
        padProjectInformationService,
        devukFieldService,
        searchSelectorService,
        pwaAreaFormValidator,
        entityCopyingService
    );

  }

  @Test
  void getActiveFieldsForApplicationDetail() {
    var pwaField = new PadLinkedArea();
    when(padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaField));
    assertThat(padAreaService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).containsExactly(pwaField);
  }

  @Test
  void updateFieldInformation_nothingEntered() {

    padAreaService.updateFieldInformation(pwaApplicationDetail, new PwaAreaForm());

    verifyNoInteractions(padAreaRepository, pwaApplicationDetailService, padProjectInformationService,
        devukFieldService);

  }

  @Test
  void updateFieldInformation_linkedToField_noField() {

    var form = new PwaAreaForm();
    form.setLinkedToArea(true);

    padAreaService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padAreaRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padAreaRepository, times(1)).deleteAll(List.of(existingField));

    verifyNoMoreInteractions(padAreaRepository, pwaApplicationDetailService, padProjectInformationService,
        devukFieldService);

  }

  @Test
  void updateFieldInformation_linkedToField_fieldSelected() {

    var form = new PwaAreaForm();
    form.setLinkedToArea(true);

    form.setLinkedAreas(List.of(String.valueOf(DEVUK_FIELD_ID),  SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName));

    var searchSelectionView = new SearchSelectionView<>(form.getLinkedAreas(),
        pickedFieldString -> devukFieldService.findById(Integer.parseInt(pickedFieldString)));
    when(devukFieldService.getLinkedAndManualFieldEntries(form.getLinkedAreas())).thenReturn(searchSelectionView);

    padAreaService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padAreaRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padAreaRepository, times(1)).deleteAll(List.of(existingField));

    verify(padAreaRepository, times(2)).saveAll(padFieldsArgumentCaptor.capture());

    var newFields = padFieldsArgumentCaptor.getAllValues();

    assertThat(newFields.size()).isEqualTo(2);

    var actualLinkedField = newFields.get(0);
    assertThat(actualLinkedField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualLinkedField.get(0).getDevukField()).isEqualTo(devukField);
    assertThat(actualLinkedField.get(0).getAreaName()).isNull();

    var actualManuallyEnteredField = newFields.get(1);
    assertThat(actualManuallyEnteredField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualManuallyEnteredField.get(0).getDevukField()).isNull();
    assertThat(actualManuallyEnteredField.get(0).getAreaName()).isEqualTo(manuallyEnteredFieldName);

    verifyNoInteractions(padProjectInformationService);

  }

  @Test
  void updateFieldInformation_notLinkedToField_noDescription() {

    var form = new PwaAreaForm();
    form.setLinkedToArea(false);

    padAreaService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padAreaRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padAreaRepository, times(1)).deleteAll(List.of(existingField));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, null);

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padAreaRepository);

  }

  @Test
  void updateFieldInformation_notLinkedToField_descriptionEntered() {

    var form = new PwaAreaForm();
    form.setLinkedToArea(false);
    form.setNoLinkedAreaDescription("description");

    padAreaService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padAreaRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padAreaRepository, times(1)).deleteAll(List.of(existingField));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, "description");

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padAreaRepository);

  }

  @Test
  void createAndSavePadFieldsFromMasterPwa_noFieldData() {

    padAreaService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, new MasterPwaDetail(), List.of());

    verifyNoInteractions(pwaApplicationDetailService, devukFieldService, padAreaRepository,
        searchSelectorService);

  }

  @Test
  void createAndSavePadFieldsFromMasterPwa_linkedToField_devukFieldAndManualEntry() {
    var pwaDetail = new MasterPwaDetail();
    pwaDetail.setLinkedToFields(true);

    var devUkFieldId = new DevukFieldId(DEVUK_FIELD_ID);
    var pwaDetailField = new MasterPwaDetailArea(pwaDetail, devUkFieldId, null);
    var pwaDetailFieldManual = new MasterPwaDetailArea(pwaDetail, null, manuallyEnteredFieldName);
    when(devukFieldService.findByDevukFieldIds(List.of(devUkFieldId))).thenReturn(List.of(devukField));

    padAreaService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, pwaDetail, List.of(pwaDetailField, pwaDetailFieldManual));

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);
    verify(padAreaRepository, times(2)).saveAll(padFieldsArgumentCaptor.capture());

    var newFields = padFieldsArgumentCaptor.getAllValues();

    assertThat(newFields.size()).isEqualTo(2);

    var actualLinkedField = newFields.get(0);
    assertThat(actualLinkedField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualLinkedField.get(0).getDevukField()).isEqualTo(devukField);
    assertThat(actualLinkedField.get(0).getAreaName()).isNull();

    var actualManuallyEnteredField = newFields.get(1);
    assertThat(actualManuallyEnteredField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualManuallyEnteredField.get(0).getDevukField()).isNull();
    assertThat(actualManuallyEnteredField.get(0).getAreaName()).isEqualTo(manuallyEnteredFieldName);

  }

  @Test
  void createAndSavePadFieldsFromMasterPwa_notLinkedToField() {

    var pwaDetail = new MasterPwaDetail();
    pwaDetail.setLinkedToFields(false);
    pwaDetail.setPwaLinkedToDescription("description");

    padAreaService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, pwaDetail, List.of());

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);
    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, "description");
    verifyNoMoreInteractions(devukFieldService, padAreaRepository);
  }

  @Test
  void mapEntityToForm_whenNoFieldData() {
    var form = new PwaAreaForm();
    when(padAreaRepository.getAllByPwaApplicationDetail(any())).thenReturn(List.of());

    padAreaService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getLinkedAreas()).isNull();
    assertThat(form.getLinkedToArea()).isNull();
    assertThat(form.getNoLinkedAreaDescription()).isNull();
  }

  @Test
  void mapEntityToForm_whenNotLinkedToField() {
    var form = new PwaAreaForm();
    var desc = "DESC";
    when(padAreaRepository.getAllByPwaApplicationDetail(any())).thenReturn(List.of());
    pwaApplicationDetail.setLinkedToArea(false);
    pwaApplicationDetail.setNotLinkedDescription(desc);

    padAreaService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getLinkedAreas()).isNull();
    assertThat(form.getLinkedToArea()).isFalse();
    assertThat(form.getNoLinkedAreaDescription()).isEqualTo(desc);
  }

  @Test
  void mapEntityToForm_whenLinkedToField() {
    var form = new PwaAreaForm();
    pwaApplicationDetail.setLinkedToArea(true);

    var padFieldManaullyEntered = new PadLinkedArea();
    padFieldManaullyEntered.setAreaName(manuallyEnteredFieldName);
    padFieldManaullyEntered.setAreaType(LinkedAreaType.FIELD);
    when(padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField, padFieldManaullyEntered));

    padAreaService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getLinkedAreas()).isEqualTo(List.of(String.valueOf(DEVUK_FIELD_ID), SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName));
    assertThat(form.getLinkedToArea()).isTrue();
    assertThat(form.getNoLinkedAreaDescription()).isNull();
  }


  @Test
  void getPreSelectedItems() {
    devukField.setFieldName("a field");
    var padFieldManaullyEntered = new PadLinkedArea();
    padFieldManaullyEntered.setAreaName(manuallyEnteredFieldName);
    when(padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField, padFieldManaullyEntered));

    Map<String, String> preSelectedItems = padAreaService.getPreSelectedApplicationFields(pwaApplicationDetail);

    assertThat(preSelectedItems).contains(
        entry(String.valueOf(DEVUK_FIELD_ID) , "a field"),
        entry(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName , manuallyEnteredFieldName));
  }


  @Test
  void isComplete_serviceInteraction_whenValidateResultAddsErrors() {

    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("linkedAreas", "linkedAreas.error");
      return invocation;
    }).when(pwaAreaFormValidator).validate(any(), any(), any(Object[].class));

    assertThat(padAreaService.isComplete(pwaApplicationDetail)).isFalse();

    verify(pwaAreaFormValidator, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());

  }

  @Test
  void isComplete_serviceInteraction_whenValidateAddsNoErrors() {

    assertThat(padAreaService.isComplete(pwaApplicationDetail)).isTrue();

    verify(pwaAreaFormValidator, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());

  }

  @Test
  void getApplicationFieldLinksView_whenNoLinkData() {
    pwaApplicationDetail.setLinkedToArea(null);
    pwaApplicationDetail.setNotLinkedDescription(null);
    var fieldLinkView = padAreaService.getApplicationAreaLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isNull();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedAreaNames()).isEmpty();

  }

  @Test
  void getApplicationFieldLinksView_whenIsNotLinkedToField() {
    pwaApplicationDetail.setLinkedToArea(false);
    pwaApplicationDetail.setNotLinkedDescription("NOT_LINKED");
    var fieldLinkView = padAreaService.getApplicationAreaLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isFalse();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(pwaApplicationDetail.getNotLinkedDescription());
    assertThat(fieldLinkView.getLinkedAreaNames()).isEmpty();

  }

  @Test
  void getApplicationFieldLinksView_whenIsLinkedToField() {
    var padManualFieldLink = new PadLinkedArea();
    padManualFieldLink.setAreaName("FIELD_NAME");

    when(padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padManualFieldLink, existingField));

    pwaApplicationDetail.setLinkedToArea(true);

    var fieldLinkView = padAreaService.getApplicationAreaLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isTrue();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedAreaNames()).containsExactly(
        new StringWithTagItem(new StringWithTag(padManualFieldLink.getAreaName(), Tag.NOT_FROM_PORTAL)),
        new StringWithTagItem(new StringWithTag(devukField.getFieldName()))
    );

  }




}

