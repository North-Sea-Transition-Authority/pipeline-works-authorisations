<#include '../../layout.ftl'>

<#-- @ftlvariable name="feePeriod" type="uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItem" -->
<#-- @ftlvariable name="feeMap" type="java.util.Map<uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType, java.util.List<uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeeItemDetail>>" -->
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

    <#list feeMap as feeType, feeItems>
        <@fdsSummaryList.summaryListWrapper summaryListId=feeType.name() + "-feeList" headingText=feeType.displayName + "s">
            <@fdsSummaryList.summaryList>
                <#list feeItems as feeItem>
                    <@fdsSummaryList.summaryListRowNoAction keyText=feeItem.getApplicationType().getDisplayName()>
                      £${feeItem.getCurrencyAmount()}
                    </@fdsSummaryList.summaryListRowNoAction>
                </#list>
            </@fdsSummaryList.summaryList>
        </@fdsSummaryList.summaryListWrapper>
    </#list>

</@defaultPage>