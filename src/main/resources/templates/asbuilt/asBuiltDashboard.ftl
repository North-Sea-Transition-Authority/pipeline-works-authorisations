<#include '../layout.ftl'>
<#include '../components/asBuiltSummary/notificationGroupSummary.ftl'>
<#include '../components/asBuiltNotificationCard/asBuiltNotificationCard.ftl'>

<@defaultPage htmlTitle="${notificationGroupSummaryView.appReference} as-built notifications" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@summary notificationGroupSummaryView=notificationGroupSummaryView titleAddOn="as-built notifications"/>

  <#if isOgaUser = true>
      <@fdsAction.link linkText="Change deadline date" linkUrl=springUrl(changeDeadlineUrl) linkClass="govuk-button govuk-button--blue" />
  </#if>
  <#list pipelineAsBuiltSubmissionViews as pipelineAsBuiltSubmissionView>
      <@asBuiltNotificationCard pipelineAsBuiltSubmissionView isOgaUser/>
  </#list>

</@defaultPage>