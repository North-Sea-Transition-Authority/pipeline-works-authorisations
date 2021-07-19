<#include '../../layout.ftl'>
<#include '../../components/asBuiltSummary/notificationGroupCheckAnswersSummary.ftl'>


<@defaultPage htmlTitle="Reopen ${notificationGroupSummaryView.appReference} as-built notifications" topNavigation=true twoThirdsColumn=true breadcrumbs=false>

    <h1 class="govuk-heading-l">Are you sure you want to reopen the as-built notifications for ${notificationGroupSummaryView.appReference}?</h1>

    <@notificationGroupCheckAnswersSummary notificationGroupSummaryView/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Reopen" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>