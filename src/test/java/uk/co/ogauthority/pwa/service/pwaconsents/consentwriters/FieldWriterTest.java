package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.fieldinformation.PwaFieldLinksView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.fieldinformation.PadFieldService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class FieldWriterTest {

  @Mock
  private MasterPwaService masterPwaService;

  @Mock
  private MasterPwaDetailFieldService masterPwaDetailFieldService;

  @Mock
  private PadFieldService padFieldService;

  private FieldWriter fieldWriter;

  private PwaApplicationDetail detail;
  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;

  private List<PadField> fields;

  @Before
  public void setUp() throws Exception {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = detail.getMasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setSourcePwaApplication(detail.getPwaApplication());

    masterPwaDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, "ref", Instant.now());

    fieldWriter = new FieldWriter(masterPwaService, masterPwaDetailFieldService, padFieldService);

    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);
    when(masterPwaService.createDuplicateNewDetail(any())).thenReturn(masterPwaDetail);

    fields = List.of(new PadField());
    when(padFieldService.getActiveFieldsForApplicationDetail(detail)).thenReturn(fields);

  }

  @Test
  public void write_initialPwa() {

    pwaConsent.setVariationNumber(0);
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.APPLICATION);

    fieldWriter.write(detail, pwaConsent);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(1)).updateDetailFieldInfo(
        masterPwaDetail,
        detail.getLinkedToField(),
        detail.getNotLinkedDescription());

    verify(masterPwaDetailFieldService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  public void write_variation_noChanges() {

    pwaConsent.setVariationNumber(1);

    var padFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));
    var pwaFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padFieldService.getApplicationFieldLinksView(detail)).thenReturn(padFieldLinksView);

    fieldWriter.write(detail, pwaConsent);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(0)).updateDetailFieldInfo(any(), any(), any());

    verify(masterPwaDetailFieldService, times(0)).createMasterPwaFieldsFromPadFields(any(), any());

  }

  @Test
  public void write_variation_changes() {

    pwaConsent.setVariationNumber(1);

    var pwaFieldLinksView = new PwaFieldLinksView(false, "desc", List.of());
    var padFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padFieldService.getApplicationFieldLinksView(detail)).thenReturn(padFieldLinksView);

    fieldWriter.write(detail, pwaConsent);

    verify(masterPwaService, times(1)).createDuplicateNewDetail(masterPwa);
    verify(masterPwaService, times(1)).updateDetailFieldInfo(masterPwaDetail, true, null);

    verify(masterPwaDetailFieldService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  public void write_depcon_changes() {

    pwaConsent.setVariationNumber(null);

    var pwaFieldLinksView = new PwaFieldLinksView(false, "desc", List.of());
    var padFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padFieldService.getApplicationFieldLinksView(detail)).thenReturn(padFieldLinksView);

    fieldWriter.write(detail, pwaConsent);

    verify(masterPwaService, times(1)).createDuplicateNewDetail(masterPwa);
    verify(masterPwaService, times(1)).updateDetailFieldInfo(masterPwaDetail, true, null);

    verify(masterPwaDetailFieldService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  public void write_depcon_noChanges() {

    pwaConsent.setVariationNumber(null);

    var padFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));
    var pwaFieldLinksView = new PwaFieldLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padFieldService.getApplicationFieldLinksView(detail)).thenReturn(padFieldLinksView);

    fieldWriter.write(detail, pwaConsent);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(0)).updateDetailFieldInfo(any(), any(), any());

    verify(masterPwaDetailFieldService, times(0)).createMasterPwaFieldsFromPadFields(any(), any());

  }

}