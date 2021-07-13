<#include '../../layout.ftl'>
<#include '../../pwaLayoutImports.ftl'>

<#macro asBuiltNotificationSummary submission historic isOgaUser summaryListClass="govuk-!-margin-bottom-9">
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
        <#else>
            <h2 class="govuk-heading-m">Submitted ${submission.submittedOnInstantDisplay} - ${submission.asBuiltGroupReference}</h2>
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
        <#if submission.dateLaid?hasContent>
            <#if submission.asBuiltNotificationStatusDisplay?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Date laid">
                    ${submission.dateLaidDisplay}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        <#elseIf submission.expectedLaidDate?hasContent>
            <#if submission.asBuiltNotificationStatusDisplay?hasContent>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Expected laid date">
                    ${submission.expectedLaidDateDisplay}
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
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="OGA submission reason">
                    ${submission.ogaSubmissionReason}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
        </#if>
    </@fdsCheckAnswers.checkAnswers>
</#macro>