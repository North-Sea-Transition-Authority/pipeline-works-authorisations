<#include '../../layout.ftl'>

<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="taskListUrl" type="String" -->
<#macro banner view canUpdate=false taskListUrl="">
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Further information requested">
          <@fdsNotificationBanner.notificationBannerContent headingText="Update due on ${view.getDeadlineTimestampDisplay()}" moreContent=moreContent>
              <p class="govuk-body">Update requested on ${view.getRequestedTimestampDisplay()}
              <@fdsDetails.summaryDetails summaryTitle="Information request summary">
                ${view.requestReason!}
              </@fdsDetails.summaryDetails>
              <#if canUpdate>
                <@fdsAction.link linkText="Update application" linkClass="govuk-button govuk-button--secondary govuk-!-margin-bottom-0" linkUrl=springUrl(taskListUrl) role=true/>
              </#if>
              </p>
          </@fdsNotificationBanner.notificationBannerContent>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>