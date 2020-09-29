package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.model.view.fieldinformation.PwaFieldLinksView;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailFieldRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;

@Service
public class MasterPwaDetailFieldService {

  private final DevukFieldService devukFieldService;
  private final MasterPwaDetailFieldRepository masterPwaDetailFieldRepository;
  private final MasterPwaDetailRepository masterPwaDetailRepository;

  @Autowired
  public MasterPwaDetailFieldService(DevukFieldService devukFieldService,
                                     MasterPwaDetailFieldRepository masterPwaDetailFieldRepository,
                                     MasterPwaDetailRepository masterPwaDetailRepository) {
    this.devukFieldService = devukFieldService;
    this.masterPwaDetailFieldRepository = masterPwaDetailFieldRepository;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
  }


  public PwaFieldLinksView getCurrentMasterPwaDetailFieldLinksView(PwaApplication pwaApplication) {
    var currentMasterPwaDetail = masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(
        pwaApplication.getMasterPwa())
        .orElseThrow(() -> new PwaEntityNotFoundException(
                "Expected to find current MasterPwaDetail. pa_id:" + pwaApplication.getId()
            )
        );

    var masterPwaDetailFields = masterPwaDetailFieldRepository.findByMasterPwaDetail(currentMasterPwaDetail);
    Map<DevukFieldId, DevukField> devukFieldLookup = devukFieldService.findByDevukFieldIds(
        masterPwaDetailFields.stream()
            .filter(o -> o.getDevukFieldId() != null)
            .map(MasterPwaDetailField::getDevukFieldId)
            .collect(Collectors.toSet())
    ).stream()
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
}
