package uk.co.ogauthority.pwa.service.masterpwas;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailFieldRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaDetailFieldServiceTest {

  private static final int FIELD_ID = 1;
  private static final String DEVUK_FIELD_NAME = "DEVUK FIELD";
  private static final String MANUAL_FIELD_NAME = "MANUAL FIELD";


  @Mock
  private DevukFieldService devukFieldService;

  @Mock
  private MasterPwaDetailFieldRepository masterPwaDetailFieldRepository;

  @Mock
  private MasterPwaService masterPwaService;

  private MasterPwaDetailFieldService masterPwaDetailFieldService;

  @Captor
  private ArgumentCaptor<List<MasterPwaDetailField>> fieldsCaptor;

  private PwaApplication pwaApplication;

  private MasterPwa masterPwa;

  private MasterPwaDetail masterPwaDetail;

  private DevukField devukField;

  @Before
  public void setUp() throws Exception {

    masterPwaDetailFieldService = new MasterPwaDetailFieldService(
        devukFieldService,
        masterPwaDetailFieldRepository,
        masterPwaService
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwaDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, "ref", Instant.now());

    when(masterPwaService.getCurrentDetailOrThrow(masterPwa))
        .thenReturn(masterPwaDetail);

    devukField = new DevukField(FIELD_ID, DEVUK_FIELD_NAME, 100);

  }

  @Test
  public void getCurrentMasterPwaDetailFieldLinksView_whenNoFieldInfoSet() {

    var fieldLinkView = masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isNull();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isNull();
    assertThat(fieldLinkView.getLinkedFieldNames()).isEmpty();

  }

  @Test
  public void getCurrentMasterPwaDetailFieldLinksView_whenNotLinkedToFields() {

    masterPwaDetail.setLinkedToFields(false);
    masterPwaDetail.setPwaLinkedToDescription("DESC");
    var fieldLinkView = masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isFalse();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(masterPwaDetail.getPwaLinkedToDescription());
    assertThat(fieldLinkView.getLinkedFieldNames()).isEmpty();

  }

  @Test
  public void getCurrentMasterPwaDetailFieldLinksView_whenIsLinkedToFields() {

    var manualFieldLink = new MasterPwaDetailField();
    manualFieldLink.setManualFieldName(MANUAL_FIELD_NAME);

    var devukFieldLink = new MasterPwaDetailField();
    devukFieldLink.setDevukFieldId(devukField.getDevukFieldId());

    when(devukFieldService.findByDevukFieldIds(Set.of(devukFieldLink.getDevukFieldId())))
        .thenReturn(List.of(devukField));

    masterPwaDetail.setLinkedToFields(true);

    when(masterPwaDetailFieldRepository.findByMasterPwaDetail(masterPwaDetail)).thenReturn(
        List.of(manualFieldLink, devukFieldLink)
    );

    var fieldLinkView = masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplication);

    assertThat(fieldLinkView.getLinkedToFields()).isTrue();
    assertThat(fieldLinkView.getPwaLinkedToDescription()).isEqualTo(masterPwaDetail.getPwaLinkedToDescription());
    assertThat(fieldLinkView.getLinkedFieldNames()).containsExactly(
        new StringWithTagItem(new StringWithTag(DEVUK_FIELD_NAME)),
        new StringWithTagItem(new StringWithTag(MANUAL_FIELD_NAME, Tag.NOT_FROM_PORTAL))
    );

  }

  @Test
  public void createMasterPwaFieldsFromPadFields() {

    var devukField = new PadField();
    devukField.setDevukField(new DevukField(1, "FNAME", 400));

    var manualField = new PadField();
    manualField.setFieldName("MANUAL");

    var detail = new MasterPwaDetail();
    masterPwaDetailFieldService.createMasterPwaFieldsFromPadFields(detail, List.of(devukField, manualField));

    verify(masterPwaDetailFieldRepository, times(1)).saveAll(fieldsCaptor.capture());

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