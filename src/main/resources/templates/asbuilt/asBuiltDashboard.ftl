<#include '../layout.ftl'>
<#include '../components/asBuiltSummary/notificationGroupSummary.ftl'>
<#include '../components/asBuiltNotificationCard/asBuiltNotificationCard.ftl'>

<@defaultPage htmlTitle="${notificationGroupSummaryView.appReference} as-built notifications" topNavigation=true twoThirdsColumn=false breadcrumbs=false>

  <@summary notificationGroupSummaryView />

    <#list pipelineAsBuiltSubmissionViews as pipelineAsBuiltSubmissionView>
        <@asBuiltNotificationCard pipelineAsBuiltSubmissionView/>
    </#list>

</@defaultPage>