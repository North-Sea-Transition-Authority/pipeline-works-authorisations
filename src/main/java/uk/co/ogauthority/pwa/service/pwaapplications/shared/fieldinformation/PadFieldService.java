package uk.co.ogauthority.pwa.service.pwaapplications.shared.fieldinformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.model.view.fieldinformation.PwaFieldLinksView;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@Service
public class PadFieldService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadFieldService.class);

  private final PadFieldRepository padFieldRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService projectInformationService;
  private final DevukFieldService devukFieldService;
  private final SearchSelectorService searchSelectorService;
  private final PwaFieldFormValidator pwaFieldFormValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadFieldService(PadFieldRepository padFieldRepository,
                         PwaApplicationDetailService pwaApplicationDetailService,
                         PadProjectInformationService projectInformationService,
                         DevukFieldService devukFieldService,
                         SearchSelectorService searchSelectorService,
                         PwaFieldFormValidator pwaFieldFormValidator,
                         EntityCopyingService entityCopyingService) {
    this.padFieldRepository = padFieldRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.projectInformationService = projectInformationService;
    this.devukFieldService = devukFieldService;
    this.searchSelectorService = searchSelectorService;
    this.pwaFieldFormValidator = pwaFieldFormValidator;
    this.entityCopyingService = entityCopyingService;
  }

  public List<PadField> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Add fields to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fields               A list of DevukFields to link to.
   */
  private void addFields(PwaApplicationDetail pwaApplicationDetail, List<DevukField> fields) {

    List<PadField> newPadFields = fields.stream()
        .map(devukField -> {
          var padField = new PadField();
          padField.setPwaApplicationDetail(pwaApplicationDetail);
          padField.setDevukField(devukField);
          return padField;
        })
        .collect(Collectors.toList());

    padFieldRepository.saveAll(newPadFields);

  }

  /**
   * Add manually entered field names to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fieldNames           A list of field names to save as PadFields.
   */
  private void addManuallyEnteredFields(PwaApplicationDetail pwaApplicationDetail, List<String> fieldNames) {

    List<PadField> newPadFields = fieldNames.stream()
        .map(fieldName -> {
          var padField = new PadField();
          padField.setPwaApplicationDetail(pwaApplicationDetail);
          padField.setFieldName(searchSelectorService.removePrefix(fieldName));
          return padField;
        })
        .collect(Collectors.toList());

    padFieldRepository.saveAll(newPadFields);

  }

  /**
   * Provides a quick way to close off all fields on the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   */
  private void removeAllFields(PwaApplicationDetail pwaApplicationDetail) {

    var fieldsToEnd = getActiveFieldsForApplicationDetail(pwaApplicationDetail);

    padFieldRepository.deleteAll(fieldsToEnd);

  }

  private void removeFdpDataFromProjectInfo(PwaApplicationDetail applicationDetail, Boolean isLinkedtoField) {
    if (!isLinkedtoField) {
      projectInformationService.removeFdpQuestionData(applicationDetail);
    }
  }

  @Transactional
  public void updateFieldInformation(PwaApplicationDetail applicationDetail, PwaFieldForm form) {

    // if they've said yes or no to the field link question, we have things to do
    if (form.getLinkedToField() != null) {

      pwaApplicationDetailService.setLinkedToFields(applicationDetail, form.getLinkedToField());

      removeAllFields(applicationDetail);

      // if they've said yes to field link and selected a field, add field
      if (form.getLinkedToField() && form.getFieldIds() != null) {

        //differentiate between existing devUkFields and manually entered field names
        var reconciledOptions = devukFieldService.getLinkedAndManualFieldEntries(form.getFieldIds());
        addFields(applicationDetail, reconciledOptions.getLinkedEntries());
        addManuallyEnteredFields(applicationDetail, reconciledOptions.getManualEntries());

      } else if (!form.getLinkedToField()) {
        // otherwise they've said no to field link, update linked field description
        pwaApplicationDetailService.setNotLinkedFieldDescription(applicationDetail, form.getNoLinkedFieldDescription());

      }

      // clear FDP answers on project info section based on whether or not we're linked to a field
      removeFdpDataFromProjectInfo(applicationDetail, form.getLinkedToField());

    }

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new PwaFieldForm();
    mapEntityToForm(detail, form);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = validate(form, bindingResult, ValidationType.FULL, detail);
    return !bindingResult.hasErrors();
  }

  public void mapEntityToForm(PwaApplicationDetail pwaApplicationDetail, PwaFieldForm form) {
    var fields = getActiveFieldsForApplicationDetail(pwaApplicationDetail);
    form.setLinkedToField(pwaApplicationDetail.getLinkedToField());

    if (!fields.isEmpty()) {
      form.setFieldIds(fields.stream()
          .map(field -> field.isLinkedToDevuk() ? field.getDevukField().getFieldId().toString()
              : SearchSelectable.FREE_TEXT_PREFIX + field.getFieldName())
          .collect(Collectors.toList())
      );

    } else {
      form.setNoLinkedFieldDescription(pwaApplicationDetail.getNotLinkedDescription());
    }
  }


  public Map<String, String> getPreSelectedApplicationFields(PwaApplicationDetail pwaApplicationDetail) {
    var fields = getActiveFieldsForApplicationDetail(pwaApplicationDetail);
    Map<String, String> preSelectedItems = new HashMap<>();

    fields.forEach(field -> {
      if (field.isLinkedToDevuk()) {
        preSelectedItems.put(field.getDevukField().getFieldId().toString(), field.getDevukField().getFieldName());
      } else {
        preSelectedItems.put(SearchSelectable.FREE_TEXT_PREFIX + field.getFieldName(), field.getFieldName());
      }
    });

    return preSelectedItems;
  }


  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    pwaFieldFormValidator.validate(form, bindingResult, validationType);
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padFieldRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadField.class
    );
  }

  public PwaFieldLinksView getApplicationFieldLinksView(PwaApplicationDetail pwaApplicationDetail) {

    var linkedFieldNames = padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .map(pf -> pf.getDevukField() != null
            ? new StringWithTag(pf.getDevukField().getFieldName())
            : new StringWithTag(pf.getFieldName(), Tag.NOT_FROM_PORTAL))
        .collect(Collectors.toList());

    return new PwaFieldLinksView(
        pwaApplicationDetail.getLinkedToField(),
        pwaApplicationDetail.getNotLinkedDescription(),
        linkedFieldNames
    );

  }

}
