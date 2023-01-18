<#include '../../layout.ftl'>

<#-- @ftlvariable name="feePeriods" type="java.util.List<uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItem>" -->
<#-- @ftlvariable name="curdate" type="java.time.Instant" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.feemanagement.controller.FeeManagementUrlFactory" -->
<#-- @ftlvariable name="newPeriodUrl" type="java.lang.String" -->
<#-- @ftlvariable name="success" type="java.lang.String"-->
<#-- @ftlvariable name="createPeriodFlag" type="java.lang.Boolean" -->


<@defaultPage htmlTitle="Fee management" pageHeading="Manage current and future fees" topNavigation=true twoThirdsColumn=true wrapperWidth=true>
    <#if success?exists>
        <#if success == "create">
            <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText="">
                <@fdsNotificationBanner.notificationBannerContent>
                    Fee period successfully created
                </@fdsNotificationBanner.notificationBannerContent>
            </@fdsNotificationBanner.notificationBannerSuccess>
        <#elseif success == "edit">
            <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText="">
                <@fdsNotificationBanner.notificationBannerContent>
                  Fee period successfully edited
                </@fdsNotificationBanner.notificationBannerContent>
            </@fdsNotificationBanner.notificationBannerSuccess>
        </#if>
    </#if>

    <#if feePeriods?has_content>
      <@fdsResultList.resultList>
        <#list feePeriods as period>
          <#assign tagName>
            <@fdsResultList.resultListTag tagText="${period.getStatus().getDisplayStatus()}" tagClass="${period.getStatus().getTagClass()}"/>
          </#assign>
          <#assign editLink>
            <@fdsAction.link linkText='Edit' linkClass='govuk-button govuk-!-margin-bottom-0 govuk-button--grey' linkUrl=springUrl(urlFactory.getFeePeriodEditUrl(period.getFeePeriodId())) role=true/>
          </#assign>
          <@fdsResultList.resultListItem
          linkHeadingUrl=springUrl(urlFactory.getFeePeriodChargesUrl(period.getFeePeriodId()))
          linkHeadingText="${period.getDescription()}"
          itemTag=tagName>
            <@fdsResultList.resultListDataItem>
              <@fdsResultList.resultListDataValue key="Start date" value="${period.getPeriodStartDisplayTime()}"/>
              <#if period.getPeriodEndTimestamp()??>
                  <@fdsResultList.resultListDataValue key="End date" value="${period.getPeriodEndDisplayTime()}"/>
              </#if>
            </@fdsResultList.resultListDataItem>
            <#if period.getStatus() == "PENDING">
              <@fdsResultList.resultListDataItem>
                <@fdsResultList.resultListDataValue key=" " value=editLink/>
              </@fdsResultList.resultListDataItem>
            </#if>
          </@fdsResultList.resultListItem>
        </#list>
      </@fdsResultList.resultList>
    </#if>
    <#if createPeriodFlag>
      <div class="govuk-warning-text govuk-!-margin-top-9">
        <span class="govuk-warning-text__icon" aria-hidden="true">i</span>
        <strong class="govuk-warning-text__text">
          <span class="govuk-warning-text__assistive">Warning</span>
          You are only allowed one future fee period at a time.
        </strong>
      </div>
      <@fdsAction.link linkText="Create new fee period" linkClass="govuk-button govuk-button--disabled" linkUrl="" role=true/>
    <#else>
      <@fdsAction.link linkText="Create new fee period" linkClass="govuk-button" linkUrl=springUrl(newPeriodUrl) role=true/>
    </#if>
</@defaultPage>