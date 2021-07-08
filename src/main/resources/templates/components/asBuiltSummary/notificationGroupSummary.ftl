<#include '../../layout.ftl'>

<#macro summary notificationGroupSummaryView showAppSummaryLink=true showAppVersionNo=false>

    <span class="govuk-caption-l">${notificationGroupSummaryView.applicationTypeDisplay}</span>
    <h1 class="govuk-heading-xl">${notificationGroupSummaryView.appReference} as-built notifications
        <br/>
            <@fdsAction.link
            linkText="View application (in new tab)"
            linkUrl=springUrl(notificationGroupSummaryView.accessLink)
            linkClass="govuk-link govuk-!-font-size-19 govuk-link--no-visited-state govuk-link--case-management-heading"
            openInNewTab=true />
    </h1>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Consent reference" value=notificationGroupSummaryView.consentReference />
        <@fdsDataItems.dataValues key="Holders" value=notificationGroupSummaryView.holder />
        <@fdsDataItems.dataValues key="As-built deadline" value=notificationGroupSummaryView.asBuiltDeadline />
    </@fdsDataItems.dataItem>

</#macro>