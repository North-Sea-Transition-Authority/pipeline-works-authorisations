package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.model.view.fieldinformation.PwaFieldLinksView;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailFieldRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;

@Service
public class MasterPwaDetailFieldService {

  private final DevukFieldService devukFieldService;
  private final MasterPwaDetailFieldRepository masterPwaDetailFieldRepository;
  private final MasterPwaService masterPwaService;

  @Autowired
  public MasterPwaDetailFieldService(DevukFieldService devukFieldService,
                                     MasterPwaDetailFieldRepository masterPwaDetailFieldRepository,
                                     MasterPwaService masterPwaService) {
    this.devukFieldService = devukFieldService;
    this.masterPwaDetailFieldRepository = masterPwaDetailFieldRepository;
    this.masterPwaService = masterPwaService;
  }

  public PwaFieldLinksView getCurrentMasterPwaDetailFieldLinksView(PwaApplication pwaApplication) {

    var currentMasterPwaDetail = masterPwaService.getCurrentDetailOrThrow(pwaApplication.getMasterPwa());

    var masterPwaDetailFields = masterPwaDetailFieldRepository.findByMasterPwaDetail(currentMasterPwaDetail);

    var fieldIds = masterPwaDetailFields.stream()
        .filter(o -> o.getDevukFieldId() != null)
        .map(MasterPwaDetailField::getDevukFieldId)
        .collect(Collectors.toSet());

    Map<DevukFieldId, DevukField> devukFieldLookup = devukFieldService.findByDevukFieldIds(fieldIds).stream()
        .collect(Collectors.toMap(DevukField::getDevukFieldId, devukField -> devukField));

    return new PwaFieldLinksView(
        currentMasterPwaDetail.getLinkedToFields(),
        currentMasterPwaDetail.getPwaLinkedToDescription(),
        masterPwaDetailFields.stream()
            .map(pwaDetailField -> pwaDetailField.getDevukFieldId() != null
                ? new StringWithTag(devukFieldLookup.get(pwaDetailField.getDevukFieldId()).getFieldName())
                : new StringWithTag(pwaDetailField.getManualFieldName(), Tag.NOT_FROM_PORTAL)
            )
        .collect(Collectors.toList())
    );

  }

  @Transactional
  public void createMasterPwaFieldsFromPadFields(MasterPwaDetail detail, List<PadField> padFields) {

    var pwaFields = padFields.stream()
        .map(padField -> createPwaFieldFromPadField(detail, padField))
        .collect(Collectors.toList());

    masterPwaDetailFieldRepository.saveAll(pwaFields);

  }

  private MasterPwaDetailField createPwaFieldFromPadField(MasterPwaDetail detail, PadField padField) {

    var devukFieldId = Optional.ofNullable(padField.getDevukField())
        .map(DevukField::getDevukFieldId)
        .orElse(null);

    return new MasterPwaDetailField(detail, devukFieldId, padField.getFieldName());

  }

}
