package uk.co.ogauthority.pwa.model.entity.publicnotice;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;

@Entity
@Table(name = "public_notice_requests")
public class PublicNoticeRequest {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "public_notice_id")
  @ManyToOne
  private PublicNotice publicNotice;

  @Enumerated(EnumType.STRING)
  private PublicNoticeRequestStatus status;

  @Enumerated(EnumType.STRING)
  private PublicNoticeRequestReason reason;

  private String reasonDescription;

  private Integer version;

  private Instant submittedTimestamp;

}
