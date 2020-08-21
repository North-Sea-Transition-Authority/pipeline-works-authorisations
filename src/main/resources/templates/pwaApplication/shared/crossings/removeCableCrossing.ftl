<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove cable crossing" pageHeading="Are you sure you want to remove this cable crossing?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <@fdsCheckAnswers.checkAnswersRow keyText="Cable name" actionText="" actionUrl="" screenReaderActionText="">
            ${cableCrossing.cableName}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Cable owner" actionText="" actionUrl="" screenReaderActionText="">
            ${cableCrossing.owner}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Location" actionText="" actionUrl="" screenReaderActionText="">
            ${cableCrossing.location}
        </@fdsCheckAnswers.checkAnswersRow>

    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove cable crossing" secondaryLinkText="Back to cable crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>