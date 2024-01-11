package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaAreaLinksView;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailAreaRepository;

@Service
public class MasterPwaDetailAreaService {

  private final DevukFieldService devukFieldService;
  private final MasterPwaDetailAreaRepository masterPwaDetailAreaRepository;
  private final MasterPwaService masterPwaService;

  @Autowired
  public MasterPwaDetailAreaService(DevukFieldService devukFieldService,
                                    MasterPwaDetailAreaRepository masterPwaDetailAreaRepository,
                                    MasterPwaService masterPwaService) {
    this.devukFieldService = devukFieldService;
    this.masterPwaDetailAreaRepository = masterPwaDetailAreaRepository;
    this.masterPwaService = masterPwaService;
  }

  public PwaAreaLinksView getCurrentMasterPwaDetailAreaLinksView(PwaApplication pwaApplication) {

    var currentMasterPwaDetail = masterPwaService.getCurrentDetailOrThrow(pwaApplication.getMasterPwa());

    var masterPwaDetailFields = masterPwaDetailAreaRepository.findByMasterPwaDetail(currentMasterPwaDetail);

    var fieldIds = masterPwaDetailFields.stream()
        .filter(o -> o.getDevukFieldId() != null)
        .map(MasterPwaDetailArea::getDevukFieldId)
        .collect(Collectors.toSet());

    Map<DevukFieldId, DevukField> devukFieldLookup = devukFieldService.findByDevukFieldIds(fieldIds).stream()
        .collect(Collectors.toMap(DevukField::getDevukFieldId, devukField -> devukField));

    return new PwaAreaLinksView(
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

  public List<MasterPwaDetailArea> getMasterPwaDetailFields(MasterPwa masterPwa) {
    var currentMasterPwaDetail = masterPwaService.getCurrentDetailOrThrow(masterPwa);
    return masterPwaDetailAreaRepository.findByMasterPwaDetail(currentMasterPwaDetail);
  }

  @Transactional
  public void createMasterPwaFieldsFromPadFields(MasterPwaDetail detail, List<PadLinkedArea> padLinkedAreas) {

    var pwaFields = padLinkedAreas.stream()
        .map(padField -> createPwaFieldFromPadField(detail, padField))
        .collect(Collectors.toList());

    masterPwaDetailAreaRepository.saveAll(pwaFields);

  }

  private MasterPwaDetailArea createPwaFieldFromPadField(MasterPwaDetail detail, PadLinkedArea padLinkedArea) {

    var devukFieldId = Optional.ofNullable(padLinkedArea.getDevukField())
        .map(DevukField::getDevukFieldId)
        .orElse(null);

    return new MasterPwaDetailArea(detail, devukFieldId, padLinkedArea.getAreaName());

  }

}
