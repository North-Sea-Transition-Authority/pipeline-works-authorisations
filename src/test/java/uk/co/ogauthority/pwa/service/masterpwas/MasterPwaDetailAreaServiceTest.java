package uk.co.ogauthority.pwa.service.masterpwas;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailAreaRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class MasterPwaDetailAreaServiceTest {

  private static final int FIELD_ID = 1;
  private static final String DEVUK_FIELD_NAME = "DEVUK FIELD";
  private static final String MANUAL_FIELD_NAME = "MANUAL FIELD";


  @Mock
  private DevukFieldService devukFieldService;

  @Mock
  private MasterPwaDetailAreaRepository masterPwaDetailAreaRepository;

  @Mock
  private MasterPwaService masterPwaService;

  private MasterPwaDetailAreaService masterPwaDetailAreaService;

  @Captor
  private ArgumentCaptor<List<MasterPwaDetailArea>> fieldsCaptor;

  private PwaApplication pwaApplication;

  private MasterPwa masterPwa;

  private MasterPwaDetail masterPwaDetail;

  private DevukField devukField;

  @BeforeEach
  void setUp() {

    masterPwaDetailAreaService = new MasterPwaDetailAreaService(
        devukFieldService,
        masterPwaDetailAreaRepository,
        masterPwaService
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwaDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, "ref", Instant.now(), PwaResourceType.PETROLEUM);

    devukField = new DevukField(FIELD_ID, DEVUK_FIELD_NAME, 100);

  }

  @Test
  void getCurrentMasterPwaDetailFieldLinksView_whenNoFieldInfoSet() {

    when(masterPwaService.getCurrentDetailOrThrow(masterPwa))
        .thenReturn(masterPwaDetail);

    var fieldLinkView = masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isNull();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedAreaNames()).isEmpty();

  }

  @Test
  void getCurrentMasterPwaDetailFieldLinksView_whenNotLinkedToFields() {

    when(masterPwaService.getCurrentDetailOrThrow(masterPwa))
        .thenReturn(masterPwaDetail);

    masterPwaDetail.setLinkedToFields(false);
    masterPwaDetail.setPwaLinkedToDescription("DESC");
    var fieldLinkView = masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isFalse();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(masterPwaDetail.getPwaLinkedToDescription());
    assertThat(fieldLinkView.getLinkedAreaNames()).isEmpty();

  }

  @Test
  void getCurrentMasterPwaDetailFieldLinksView_whenIsLinkedToFields() {

    when(masterPwaService.getCurrentDetailOrThrow(masterPwa))
        .thenReturn(masterPwaDetail);

    var manualFieldLink = new MasterPwaDetailArea();
    manualFieldLink.setManualFieldName(MANUAL_FIELD_NAME);

    var devukFieldLink = new MasterPwaDetailArea();
    devukFieldLink.setDevukFieldId(devukField.getDevukFieldId());

    when(devukFieldService.findByDevukFieldIds(Set.of(devukFieldLink.getDevukFieldId())))
        .thenReturn(List.of(devukField));

    masterPwaDetail.setLinkedToFields(true);

    when(masterPwaDetailAreaRepository.findByMasterPwaDetail(masterPwaDetail)).thenReturn(
        List.of(manualFieldLink, devukFieldLink)
    );

    var fieldLinkView = masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isTrue();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(masterPwaDetail.getPwaLinkedToDescription());
    assertThat(fieldLinkView.getLinkedAreaNames()).containsExactly(
        new StringWithTagItem(new StringWithTag(DEVUK_FIELD_NAME)),
        new StringWithTagItem(new StringWithTag(MANUAL_FIELD_NAME, Tag.NOT_FROM_PORTAL))
    );

  }

  @Test
  void createMasterPwaFieldsFromPadFields() {

    var devukField = new PadLinkedArea();
    devukField.setDevukField(new DevukField(1, "FNAME", 400));

    var manualField = new PadLinkedArea();
    manualField.setAreaName("MANUAL");

    var detail = new MasterPwaDetail();
    masterPwaDetailAreaService.createMasterPwaFieldsFromPadFields(detail, List.of(devukField, manualField));

    verify(masterPwaDetailAreaRepository, times(1)).saveAll(fieldsCaptor.capture());

    assertThat(fieldsCaptor.getValue().get(0)).satisfies(field -> {
      assertThat(field.getDevukFieldId()).isEqualTo(devukField.getDevukField().getDevukFieldId());
      assertThat(field.getMasterPwaDetail()).isEqualTo(detail);
      assertThat(field.getManualFieldName()).isNull();
    });

    assertThat(fieldsCaptor.getValue().get(1)).satisfies(field -> {
      assertThat(field.getDevukFieldId()).isNull();
      assertThat(field.getMasterPwaDetail()).isEqualTo(detail);
      assertThat(field.getManualFieldName()).isEqualTo("MANUAL");
    });

  }

}
