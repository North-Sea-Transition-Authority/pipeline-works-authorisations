package uk.co.ogauthority.pwa.service.devuk;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@Service
public class PadFieldService {

  private final PadFieldRepository padFieldRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService projectInformationService;
  private final DevukFieldService devukFieldService;

  @Autowired
  public PadFieldService(PadFieldRepository padFieldRepository,
                         PwaApplicationDetailService pwaApplicationDetailService,
                         PadProjectInformationService projectInformationService,
                         DevukFieldService devukFieldService) {
    this.padFieldRepository = padFieldRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.projectInformationService = projectInformationService;
    this.devukFieldService = devukFieldService;
  }

  public List<PadField> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Add fields to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fields A list of DevukFields to link to.
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
      if (form.getLinkedToField() && form.getFieldId() != null) {

        var devukField = devukFieldService.findById(form.getFieldId());
        addFields(applicationDetail, List.of(devukField));

      } else if (!form.getLinkedToField()) {

        // otherwise they've said no to field link, update linked field description
        pwaApplicationDetailService.setNotLinkedFieldDescription(applicationDetail, form.getNoLinkedFieldDescription());

      }

      // clear FDP answers on project info section based on whether or not we're linked to a field
      removeFdpDataFromProjectInfo(applicationDetail, form.getLinkedToField());

    }

  }
}
