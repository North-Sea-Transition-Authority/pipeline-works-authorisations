package uk.co.ogauthority.pwa.service.masterpwas;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailFieldRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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
  private MasterPwaDetailRepository masterPwaDetailRepository;


  private MasterPwaDetailFieldService masterPwaDetailFieldService;

  private PwaApplication pwaApplication;

  private MasterPwa masterPwa;

  private MasterPwaDetail masterPwaDetail;

  private DevukField devukField;

  @Before
  public void setUp() throws Exception {

    masterPwaDetailFieldService = new MasterPwaDetailFieldService(
        devukFieldService,
        masterPwaDetailFieldRepository,
        masterPwaDetailRepository
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
    masterPwaDetail = new MasterPwaDetail(Instant.now());
    masterPwaDetail.setMasterPwa(masterPwa);

    when(masterPwaDetailRepository.findByMasterPwaAndEndInstantIsNull(masterPwa))
        .thenReturn(Optional.of(masterPwaDetail));

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
}