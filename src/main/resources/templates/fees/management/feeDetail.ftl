<#include '../../layout.ftl'>

<#-- @ftlvariable name="feePeriod" type="uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItem" -->
<#-- @ftlvariable name="DefaultFees" type="java.util.List<uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeeItemDetail>" -->
<#-- @ftlvariable name="FastTrackFees" type="java.util.List<uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeeItemDetail>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String"-->


<@defaultPage
  htmlTitle="Fee management"
  pageHeading="${feePeriod.getDescription()}"
  topNavigation=true
  twoThirdsColumn=true
  wrapperWidth=true
  backLink=true
  backLinkUrl=springUrl(backUrl)>
  <@fdsSummaryList.summaryListWrapper summaryListId="standardFeeList" headingText="Applications fees">
    <@fdsSummaryList.summaryList>
      <#list DefaultFees as feeDetail>
          <@fdsSummaryList.summaryListRowNoAction keyText="${feeDetail.getApplicationType().getDisplayName()}">
            £${feeDetail.getCurrencyAmount()}
          </@fdsSummaryList.summaryListRowNoAction>
      </#list>
    </@fdsSummaryList.summaryList>
  </@fdsSummaryList.summaryListWrapper>
    <@fdsSummaryList.summaryListWrapper summaryListId="fasttrackSurchargeFeeList" headingText="FastTrack Surcharges">
        <@fdsSummaryList.summaryList>
            <#list FastTrackFees as feeDetail>
                <@fdsSummaryList.summaryListRowNoAction keyText="${feeDetail.getApplicationType().getDisplayName()}">
                    £${feeDetail.getCurrencyAmount()}
                </@fdsSummaryList.summaryListRowNoAction>
            </#list>
        </@fdsSummaryList.summaryList>
    </@fdsSummaryList.summaryListWrapper>
</@defaultPage>