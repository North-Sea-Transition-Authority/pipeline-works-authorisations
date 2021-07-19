<#include '../../layout.ftl'>
<#include '../../pwaLayoutImports.ftl'>

<#macro notificationGroupCheckAnswersSummary notificationGroupSummaryView>
    <@fdsCheckAnswers.checkAnswers>
        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Consent reference">
            ${notificationGroupSummaryView.consentReference}
        </@fdsCheckAnswers.checkAnswersRowNoAction>

        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Holders">
            ${notificationGroupSummaryView.holder}
        </@fdsCheckAnswers.checkAnswersRowNoAction>

        <@fdsCheckAnswers.checkAnswersRowNoAction keyText="As-built deadline">
            ${notificationGroupSummaryView.asBuiltDeadline}
        </@fdsCheckAnswers.checkAnswersRowNoAction>
    </@fdsCheckAnswers.checkAnswers>
</#macro>