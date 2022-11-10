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
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchResult;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectionView;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RunWith(MockitoJUnitRunner.class)
public class PadFieldServiceTest {
  private final int DEVUK_FIELD_ID = 1;
  private final String DEVUK_FIELD_NAME = "FIELD_NAME";

  @Mock
  private PadFieldRepository padFieldRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private DevukFieldService devukFieldService;

  @Mock
  private SearchSelectorService searchSelectorService;

  @Mock
  private PwaFieldFormValidator pwaFieldFormValidator;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Captor
  private ArgumentCaptor<List<PadField>> padFieldsArgumentCaptor;

  private PadFieldService padFieldService;
  private PwaApplicationDetail pwaApplicationDetail;

  private PadField existingField;
  private DevukField devukField;
  private String manuallyEnteredFieldName = "my entered field";

  @Before
  public void setUp() {

    pwaApplicationDetail = new PwaApplicationDetail();

    devukField = new DevukField(DEVUK_FIELD_ID, DEVUK_FIELD_NAME, 100);

    existingField = new PadField();
    existingField.setPwaApplicationDetail(pwaApplicationDetail);
    existingField.setDevukField(devukField);

    when(searchSelectorService.removePrefix(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName)).thenReturn(manuallyEnteredFieldName);

    when(devukFieldService.findById(DEVUK_FIELD_ID)).thenReturn(devukField);

    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField));

    padFieldService = new PadFieldService(
        padFieldRepository,
        pwaApplicationDetailService,
        padProjectInformationService,
        devukFieldService,
        searchSelectorService,
        pwaFieldFormValidator,
        entityCopyingService
    );

  }

  @Test
  public void getActiveFieldsForApplicationDetail() {
    var pwaField = new PadField();
    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(pwaField));
    assertThat(padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).containsExactly(pwaField);
  }

  @Test
  public void updateFieldInformation_nothingEntered() {

    padFieldService.updateFieldInformation(pwaApplicationDetail, new PwaFieldForm());

    verifyNoInteractions(padFieldRepository, pwaApplicationDetailService, padProjectInformationService,
        devukFieldService);

  }

  @Test
  public void updateFieldInformation_linkedToField_noField() {

    var form = new PwaFieldForm();
    form.setLinkedToField(true);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(List.of(existingField));

    verifyNoMoreInteractions(padFieldRepository, pwaApplicationDetailService, padProjectInformationService,
        devukFieldService);

  }

  @Test
  public void updateFieldInformation_linkedToField_fieldSelected() {

    var form = new PwaFieldForm();
    form.setLinkedToField(true);

    form.setFieldIds(List.of(
        new SearchResult(String.valueOf(DEVUK_FIELD_ID)),
        new SearchResult(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName)
    ));

    var searchSelectionView = new SearchSelectionView<>(
        form.getFieldIds().stream().map(SearchResult::getValue).collect(Collectors.toList()),
        pickedFieldString -> devukFieldService.findById(Integer.parseInt(pickedFieldString))
    );
    when(devukFieldService.getLinkedAndManualFieldEntries(form.getFieldIds())).thenReturn(searchSelectionView);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(List.of(existingField));

    verify(padFieldRepository, times(2)).saveAll(padFieldsArgumentCaptor.capture());

    var newFields = padFieldsArgumentCaptor.getAllValues();

    assertThat(newFields.size()).isEqualTo(2);

    var actualLinkedField = newFields.get(0);
    assertThat(actualLinkedField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualLinkedField.get(0).getDevukField()).isEqualTo(devukField);
    assertThat(actualLinkedField.get(0).getFieldName()).isNull();

    var actualManuallyEnteredField = newFields.get(1);
    assertThat(actualManuallyEnteredField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualManuallyEnteredField.get(0).getDevukField()).isNull();
    assertThat(actualManuallyEnteredField.get(0).getFieldName()).isEqualTo(manuallyEnteredFieldName);

    verifyNoInteractions(padProjectInformationService);

  }

  @Test
  public void updateFieldInformation_notLinkedToField_noDescription() {

    var form = new PwaFieldForm();
    form.setLinkedToField(false);

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(List.of(existingField));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, null);

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padFieldRepository);

  }

  @Test
  public void updateFieldInformation_notLinkedToField_descriptionEntered() {

    var form = new PwaFieldForm();
    form.setLinkedToField(false);
    form.setNoLinkedFieldDescription("description");

    padFieldService.updateFieldInformation(pwaApplicationDetail, form);

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);

    verify(padFieldRepository, times(1)).getAllByPwaApplicationDetail(pwaApplicationDetail);
    verify(padFieldRepository, times(1)).deleteAll(List.of(existingField));

    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, "description");

    verify(padProjectInformationService, times(1)).removeFdpQuestionData(pwaApplicationDetail);

    verifyNoMoreInteractions(devukFieldService, padFieldRepository);

  }

  @Test
  public void createAndSavePadFieldsFromMasterPwa_noFieldData() {

    padFieldService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, new MasterPwaDetail(), List.of());

    verifyNoInteractions(pwaApplicationDetailService, devukFieldService, padFieldRepository,
        searchSelectorService);

  }

  @Test
  public void createAndSavePadFieldsFromMasterPwa_linkedToField_devukFieldAndManualEntry() {
    var pwaDetail = new MasterPwaDetail();
    pwaDetail.setLinkedToFields(true);

    var devUkFieldId = new DevukFieldId(DEVUK_FIELD_ID);
    var pwaDetailField = new MasterPwaDetailField(pwaDetail, devUkFieldId, null);
    var pwaDetailFieldManual = new MasterPwaDetailField(pwaDetail, null, manuallyEnteredFieldName);
    when(devukFieldService.findByDevukFieldIds(List.of(devUkFieldId))).thenReturn(List.of(devukField));

    padFieldService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, pwaDetail, List.of(pwaDetailField, pwaDetailFieldManual));

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, true);
    verify(padFieldRepository, times(2)).saveAll(padFieldsArgumentCaptor.capture());

    var newFields = padFieldsArgumentCaptor.getAllValues();

    assertThat(newFields.size()).isEqualTo(2);

    var actualLinkedField = newFields.get(0);
    assertThat(actualLinkedField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualLinkedField.get(0).getDevukField()).isEqualTo(devukField);
    assertThat(actualLinkedField.get(0).getFieldName()).isNull();

    var actualManuallyEnteredField = newFields.get(1);
    assertThat(actualManuallyEnteredField.get(0).getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(actualManuallyEnteredField.get(0).getDevukField()).isNull();
    assertThat(actualManuallyEnteredField.get(0).getFieldName()).isEqualTo(manuallyEnteredFieldName);

  }

  @Test
  public void createAndSavePadFieldsFromMasterPwa_notLinkedToField() {

    var pwaDetail = new MasterPwaDetail();
    pwaDetail.setLinkedToFields(false);
    pwaDetail.setPwaLinkedToDescription("description");

    padFieldService.createAndSavePadFieldsFromMasterPwa(pwaApplicationDetail, pwaDetail, List.of());

    verify(pwaApplicationDetailService, times(1)).setLinkedToFields(pwaApplicationDetail, false);
    verify(pwaApplicationDetailService, times(1)).setNotLinkedFieldDescription(pwaApplicationDetail, "description");
    verifyNoMoreInteractions(devukFieldService, padFieldRepository);
  }

  @Test
  public void mapEntityToForm_whenNoFieldData() {
    var form = new PwaFieldForm();
    when(padFieldRepository.getAllByPwaApplicationDetail(any())).thenReturn(List.of());

    padFieldService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getFieldIds()).isNull();
    assertThat(form.getLinkedToField()).isNull();
    assertThat(form.getNoLinkedFieldDescription()).isNull();
  }

  @Test
  public void mapEntityToForm_whenNotLinkedToField() {
    var form = new PwaFieldForm();
    var desc = "DESC";
    when(padFieldRepository.getAllByPwaApplicationDetail(any())).thenReturn(List.of());
    pwaApplicationDetail.setLinkedToField(false);
    pwaApplicationDetail.setNotLinkedDescription(desc);

    padFieldService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getFieldIds()).isNull();
    assertThat(form.getLinkedToField()).isFalse();
    assertThat(form.getNoLinkedFieldDescription()).isEqualTo(desc);
  }

  @Test
  public void mapEntityToForm_whenLinkedToField() {
    var form = new PwaFieldForm();
    pwaApplicationDetail.setLinkedToField(true);

    var padFieldManaullyEntered = new PadField();
    padFieldManaullyEntered.setFieldName(manuallyEnteredFieldName);
    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField, padFieldManaullyEntered));

    padFieldService.mapEntityToForm(pwaApplicationDetail, form);

    assertThat(form.getFieldIds()).isEqualTo(List.of(
        new SearchResult(String.valueOf(DEVUK_FIELD_ID)),
        new SearchResult(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName))
    );
    assertThat(form.getLinkedToField()).isTrue();
    assertThat(form.getNoLinkedFieldDescription()).isNull();
  }


  @Test
  public void getPreSelectedItems() {
    devukField.setFieldName("a field");
    var padFieldManaullyEntered = new PadField();
    padFieldManaullyEntered.setFieldName(manuallyEnteredFieldName);
    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(existingField, padFieldManaullyEntered));

    Map<String, String> preSelectedItems = padFieldService.getPreSelectedApplicationFields(pwaApplicationDetail);

    assertThat(preSelectedItems).contains(
        entry(String.valueOf(DEVUK_FIELD_ID) , "a field"),
        entry(SearchSelectable.FREE_TEXT_PREFIX + manuallyEnteredFieldName , manuallyEnteredFieldName));
  }



  @Test
  public void isComplete_serviceInteraction_whenValidateResultAddsErrors() {

    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("fieldIds", "fieldIds.error");
      return invocation;
    }).when(pwaFieldFormValidator).validate(any(), any(), any());

    assertThat(padFieldService.isComplete(pwaApplicationDetail)).isFalse();

    verify(pwaFieldFormValidator, times(1)).validate(any(), any(), eq(ValidationType.FULL) );

  }

  @Test
  public void isComplete_serviceInteraction_whenValidateAddsNoErrors() {

    assertThat(padFieldService.isComplete(pwaApplicationDetail)).isTrue();

    verify(pwaFieldFormValidator, times(1)).validate(any(), any(), eq(ValidationType.FULL) );

  }

  @Test
  public void getApplicationFieldLinksView_whenNoLinkData() {
    pwaApplicationDetail.setLinkedToField(null);
    pwaApplicationDetail.setNotLinkedDescription(null);
    var fieldLinkView = padFieldService.getApplicationFieldLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isNull();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedFieldNames()).isEmpty();

  }

  @Test
  public void getApplicationFieldLinksView_whenIsNotLinkedToField() {
    pwaApplicationDetail.setLinkedToField(false);
    pwaApplicationDetail.setNotLinkedDescription("NOT_LINKED");
    var fieldLinkView = padFieldService.getApplicationFieldLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isFalse();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(pwaApplicationDetail.getNotLinkedDescription());
    assertThat(fieldLinkView.getLinkedFieldNames()).isEmpty();

  }

  @Test
  public void getApplicationFieldLinksView_whenIsLinkedToField() {
    var padManualFieldLink = new PadField();
    padManualFieldLink.setFieldName("FIELD_NAME");

    when(padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padManualFieldLink, existingField));

    pwaApplicationDetail.setLinkedToField(true);

    var fieldLinkView = padFieldService.getApplicationFieldLinksView(pwaApplicationDetail);

    assertThat(fieldLinkView.getLinkedToFields()).isTrue();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedFieldNames()).containsExactly(
        new StringWithTagItem(new StringWithTag(padManualFieldLink.getFieldName(), Tag.NOT_FROM_PORTAL)),
        new StringWithTagItem(new StringWithTag(devukField.getFieldName()))
    );

  }




}