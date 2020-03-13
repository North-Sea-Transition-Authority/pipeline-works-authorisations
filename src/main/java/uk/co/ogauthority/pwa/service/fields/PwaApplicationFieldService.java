package uk.co.ogauthority.pwa.service.fields;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.fields.DevukField;
import uk.co.ogauthority.pwa.model.entity.fields.PwaApplicationDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.fields.PwaFieldRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class PwaApplicationFieldService {

  private final PwaFieldRepository pwaFieldRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public PwaApplicationFieldService(PwaFieldRepository pwaFieldRepository,
                                    PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaFieldRepository = pwaFieldRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  public List<PwaApplicationDetailField> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return pwaFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Adds a field to the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   * @param devukField a DEVUK field.
   * @return The new PwaField.
   */
  @Transactional
  public PwaApplicationDetailField addField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField) {
    var newField = new PwaApplicationDetailField();
    newField.setDevukField(devukField);
    newField.setPwaApplicationDetail(pwaApplicationDetail);
    pwaFieldRepository.save(newField);
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
  public PwaApplicationDetailField removeField(PwaApplicationDetail pwaApplicationDetail, DevukField devukField) {
    var oldField = pwaFieldRepository.findByPwaApplicationDetailAndDevukField(pwaApplicationDetail, devukField);
    pwaFieldRepository.delete(oldField);
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
  public List<PwaApplicationDetailField> setFields(PwaApplicationDetail pwaApplicationDetail, List<DevukField> fields) {

    removeAllFields(pwaApplicationDetail);

    pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, fields.size() != 0);
    var addedFields = new ArrayList<PwaApplicationDetailField>();
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
  public List<PwaApplicationDetailField> removeAllFields(PwaApplicationDetail pwaApplicationDetail) {
    var endedFields = new ArrayList<PwaApplicationDetailField>();
    getActiveFieldsForApplicationDetail(pwaApplicationDetail).forEach(
        pwaApplicationDetailField -> {
          var removedField = removeField(pwaApplicationDetail, pwaApplicationDetailField.getDevukField());
          endedFields.add(removedField);
        }
    );
    return endedFields;
  }

}
