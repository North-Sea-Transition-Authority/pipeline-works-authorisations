package uk.co.ogauthority.pwa.service.devuk;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@Service
public class PadFieldService {

  private final PadFieldRepository padFieldRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService projectInformationService;

  @Autowired
  public PadFieldService(PadFieldRepository padFieldRepository,
                         PwaApplicationDetailService pwaApplicationDetailService,
                         PadProjectInformationService projectInformationService) {
    this.padFieldRepository = padFieldRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.projectInformationService = projectInformationService;
  }

  public List<PadField> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Adds a field to the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   * @param devukField a DEVUK field.
   * @return The new PwaField.
   */
  @Transactional
  public PadField addField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField) {
    var newField = new PadField();
    newField.setDevukField(devukField);
    newField.setPwaApplicationDetail(pwaApplicationDetail);
    padFieldRepository.save(newField);
    return newField;
  }

  /**
   * Remove the field from the application's field history.
   *
   * @param pwaApplicationDetail Current application detail.
   * @param devukField a DEVUK field.
   * @return The removed field.
   */
  @Transactional
  public PadField removeField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField) {
    var oldField = padFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukField);
    padFieldRepository.delete(oldField);
    return oldField;
  }

  /**
   * Link fields to an application detail.
   * If fields, set detail to be linked to fields. If empty list, set linked to false.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fields A list of DevukFields to link to.
   * @return List of added PwaApplicationDetailFields.
   */
  @Transactional
  public List<PadField> setFields(PwaApplicationDetail pwaApplicationDetail, List<DevukField> fields) {

    removeAllFields(pwaApplicationDetail);

    pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, fields.size() != 0);
    var addedFields = new ArrayList<PadField>();
    fields.forEach(devukField -> {
      var addedField = addField(pwaApplicationDetail, devukField);
      addedFields.add(addedField);
    });
    return addedFields;
  }

  /**
   * Provides a quick way to close off all fields on the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   * @return Returns a list of the ended fields.
   */
  @Transactional
  public List<PadField> removeAllFields(PwaApplicationDetail pwaApplicationDetail) {
    var endedFields = new ArrayList<PadField>();
    getActiveFieldsForApplicationDetail(pwaApplicationDetail).forEach(
        padField -> {
          var removedField = removeField(pwaApplicationDetail, padField.getDevukField());
          endedFields.add(removedField);
        }
    );
    return endedFields;
  }

  public void removeFdpDataFromProjectInfo(Boolean isLinkedtoField, PwaApplicationDetail applicationDetail) {
    if (!isLinkedtoField) {
      projectInformationService.removeFdpQuestionData(applicationDetail);
    }
  }

}
