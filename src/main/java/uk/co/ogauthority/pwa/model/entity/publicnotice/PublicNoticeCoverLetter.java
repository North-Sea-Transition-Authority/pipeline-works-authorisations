package uk.co.ogauthority.pwa.model.entity.publicnotice;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.CoverLetterTextType;

@Entity
@Table(name = "public_notice_cover_letters")
public class PublicNoticeCoverLetter {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  private CoverLetterTextType textType;

  private String text;

  private Instant endTimestamp;



}
