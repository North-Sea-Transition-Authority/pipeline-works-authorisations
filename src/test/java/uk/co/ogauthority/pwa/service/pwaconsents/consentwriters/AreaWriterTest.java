package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaAreaLinksView;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailAreaService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AreaWriterTest {

  @Mock
  private MasterPwaService masterPwaService;

  @Mock
  private MasterPwaDetailAreaService masterPwaDetailAreaService;

  @Mock
  private PadAreaService padAreaService;

  private AreaWriter areaWriter;

  private PwaApplicationDetail detail;
  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;

  private List<PadLinkedArea> fields;

  private ConsentWriterDto consentWriterDto;

  @BeforeEach
  void setUp() throws Exception {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = detail.getMasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setSourcePwaApplication(detail.getPwaApplication());

    masterPwaDetail = new MasterPwaDetail(masterPwa, MasterPwaDetailStatus.CONSENTED, "ref", Instant.now(), PwaResourceType.PETROLEUM);

    areaWriter = new AreaWriter(masterPwaService, masterPwaDetailAreaService, padAreaService);

    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);
    when(masterPwaService.createDuplicateNewDetail(any())).thenReturn(masterPwaDetail);

    fields = List.of(new PadLinkedArea());
    when(padAreaService.getActiveFieldsForApplicationDetail(detail)).thenReturn(fields);

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  void write_initialPwa() {

    pwaConsent.setVariationNumber(0);
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.APPLICATION);

    areaWriter.write(detail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(1)).updateDetailFieldInfo(
        masterPwaDetail,
        detail.getLinkedToArea(),
        detail.getNotLinkedDescription());

    verify(masterPwaDetailAreaService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  void write_variation_noChanges() {

    pwaConsent.setVariationNumber(1);

    var padFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));
    var pwaFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padAreaService.getApplicationAreaLinksView(detail)).thenReturn(padFieldLinksView);

    areaWriter.write(detail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(0)).updateDetailFieldInfo(any(), any(), any());

    verify(masterPwaDetailAreaService, times(0)).createMasterPwaFieldsFromPadFields(any(), any());

  }

  @Test
  void write_variation_changes() {

    pwaConsent.setVariationNumber(1);

    var pwaFieldLinksView = new PwaAreaLinksView(false, "desc", List.of());
    var padFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padAreaService.getApplicationAreaLinksView(detail)).thenReturn(padFieldLinksView);

    areaWriter.write(detail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(1)).createDuplicateNewDetail(masterPwa);
    verify(masterPwaService, times(1)).updateDetailFieldInfo(masterPwaDetail, true, null);

    verify(masterPwaDetailAreaService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  void write_depcon_changes() {

    pwaConsent.setVariationNumber(null);

    var pwaFieldLinksView = new PwaAreaLinksView(false, "desc", List.of());
    var padFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padAreaService.getApplicationAreaLinksView(detail)).thenReturn(padFieldLinksView);

    areaWriter.write(detail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(1)).createDuplicateNewDetail(masterPwa);
    verify(masterPwaService, times(1)).updateDetailFieldInfo(masterPwaDetail, true, null);

    verify(masterPwaDetailAreaService, times(1)).createMasterPwaFieldsFromPadFields(masterPwaDetail, fields);

  }

  @Test
  void write_depcon_noChanges() {

    pwaConsent.setVariationNumber(null);

    var padFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));
    var pwaFieldLinksView = new PwaAreaLinksView(true, null, List.of(new StringWithTag("fieldname")));

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(detail.getPwaApplication()))
        .thenReturn(pwaFieldLinksView);

    when(padAreaService.getApplicationAreaLinksView(detail)).thenReturn(padFieldLinksView);

    areaWriter.write(detail, pwaConsent, consentWriterDto);

    verify(masterPwaService, times(0)).createDuplicateNewDetail(any());
    verify(masterPwaService, times(0)).updateDetailFieldInfo(any(), any(), any());

    verify(masterPwaDetailAreaService, times(0)).createMasterPwaFieldsFromPadFields(any(), any());

  }

}
