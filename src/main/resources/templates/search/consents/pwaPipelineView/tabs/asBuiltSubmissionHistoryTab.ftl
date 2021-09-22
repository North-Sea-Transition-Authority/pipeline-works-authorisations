<#include '../../../../layout.ftl'>
<#include '../../../../pwaLayoutImports.ftl'>
<#include '../../../../components/asBuiltSummary/asBuiltNotificationSummary.ftl'>

<#macro tab submissionHistoryView isOgaUser>

    <#if submissionHistoryView.latestSubmissionView??>
        <#assign latestSubmissionView = submissionHistoryView.latestSubmissionView/>
        <h2 class="govuk-heading-m">Current as-built information</h2>
        <@asBuiltNotificationSummary submission=latestSubmissionView historic=false isOgaUser=isOgaUser/>

        <#if submissionHistoryView.historicalSubmissionViews?hasContent>
            <@fdsDetails.summaryDetails summaryTitle="Show previous submitted as-built notifications">
                <#list submissionHistoryView.historicalSubmissionViews as historicalSubmissionView>
                    <#if historicalSubmissionView?hasNext>
                        <@asBuiltNotificationSummary submission=historicalSubmissionView historic=true isOgaUser=isOgaUser/>
                    <#else>
                        <@asBuiltNotificationSummary submission=historicalSubmissionView historic=true isOgaUser=isOgaUser summaryListClass="govuk-!-margin-bottom-0"/>
                    </#if>
                </#list>
             </@fdsDetails.summaryDetails>
        <#else>
            <@fdsInsetText.insetText>There are no previous submitted as-built notifications for this pipeline</@fdsInsetText.insetText>
        </#if>
    <#else>
        <div class="govuk-inset-text govuk-!-margin-bottom-0">
            There are no submitted as-built notifications for this pipeline.
        </div>
    </#if>

</#macro>