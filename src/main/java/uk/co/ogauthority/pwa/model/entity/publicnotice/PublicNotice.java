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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Entity
@Table(name = "public_notices")
public class PublicNotice {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_id")
  @ManyToOne
  private PwaApplication pwaApplication;

  @Enumerated(EnumType.STRING)
  private PublicNoticeStatus status;

  @JoinColumn(name = "cover_letter_id")
  @OneToOne
  private PublicNoticeCoverLetter coverLetter;

  private Integer version;

  private Instant publicationStartTimestamp;
  private Instant publicationEndTimestamp;
  private Instant submittedTimestamp;

}
