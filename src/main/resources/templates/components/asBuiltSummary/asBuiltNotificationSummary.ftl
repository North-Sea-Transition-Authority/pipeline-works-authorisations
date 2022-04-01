<#include '../../layout.ftl'>
<#include '../../pwaLayoutImports.ftl'>

<#macro asBuiltNotificationSummary submission historic isOgaUser summaryListClass="govuk-!-margin-bottom-9">
    <#if historic >
        <h2 class="govuk-heading-m">Submitted ${submission.submittedOnInstantDisplay} - ${submission.asBuiltGroupReference}</h2>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass>
        <#if historic == false>
            <#if submission.asBuiltGroupReference?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="As-built application reference">
                    ${submission.asBuiltGroupReference}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
            <#if submission.submittedByPersonName?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Submitted on">
                    ${submission.submittedOnInstantDisplay}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        </#if>
        <#if submission.submittedByPersonName?hasContent>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Submitted by">
                ${submission.submittedByPersonName} (${submission.submittedByPersonEmail})
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </#if>
        <#if submission.asBuiltNotificationStatusDisplay?hasContent>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="As-built status">
                ${submission.asBuiltNotificationStatusDisplay}
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </#if>
        <#if submission.dateWorkCompleted?hasContent>
            <#if submission.asBuiltNotificationStatusDisplay?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Date work completed">
                    ${submission.dateWorkCompletedDisplay}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        <#elseIf submission.expectedWorkCompletedDate?hasContent>
            <#if submission.asBuiltNotificationStatusDisplay?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Expected date work will be completed">
                    ${submission.expectedWorkCompletedDateDisplay}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        </#if>
        <#if submission.dateBroughtIntoUse?hasContent>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Date pipeline was/will be brought into use">
                ${submission.dateBroughtIntoUseDisplay}
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </#if>
        <#if isOgaUser == true>
            <#if submission.ogaSubmissionReason?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="NSTA submission reason">
                    ${submission.ogaSubmissionReason}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        </#if>
    </@fdsCheckAnswers.checkAnswers>
</#macro>