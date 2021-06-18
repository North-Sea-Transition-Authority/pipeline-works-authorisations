<#include '../layout.ftl'>
<#include '../components/asBuiltSummary/notificationGroupSummary.ftl'>
<#include '../components/asBuiltNotificationCard/asBuiltNotificationCard.ftl'>

<@defaultPage htmlTitle="${notificationGroupSummaryView.appReference} as-built notifications" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@summary notificationGroupSummaryView />

  <#if isOgaUser = true>
      <@fdsAction.link linkText="Change deadline date" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(changeDeadlineUrl)/>
  </#if>
  <#list pipelineAsBuiltSubmissionViews as pipelineAsBuiltSubmissionView>
      <@asBuiltNotificationCard pipelineAsBuiltSubmissionView/>
  </#list>

</@defaultPage>