package uk.co.ogauthority.pwa.model.view.publicnotice;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;

public class AllPublicNoticesView {


  private final PublicNoticeView currentPublicNotice;
  private final List<PublicNoticeView> historicalPublicNotices;
  private final Set<PublicNoticeAction> actions;

  public AllPublicNoticesView(PublicNoticeView currentPublicNotice,
                              List<PublicNoticeView> historicalPublicNotices,
                              Set<PublicNoticeAction> actions) {
    this.currentPublicNotice = currentPublicNotice;
    this.historicalPublicNotices = historicalPublicNotices;
    this.actions = actions;
  }


  public PublicNoticeView getCurrentPublicNotice() {
    return currentPublicNotice;
  }

  public List<PublicNoticeView> getHistoricalPublicNotices() {
    return historicalPublicNotices;
  }

  public Set<PublicNoticeAction> getActions() {
    return actions;
  }

  public boolean hasPublicNotices() {
    return currentPublicNotice != null || !historicalPublicNotices.isEmpty();
  }
}
