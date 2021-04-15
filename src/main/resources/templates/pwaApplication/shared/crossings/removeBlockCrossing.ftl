<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove block crossing" pageHeading="Are you sure you want to remove this block crossing?" breadcrumbs=true errorItems=errorList>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <@fdsCheckAnswers.checkAnswersRow keyText="UK block reference" actionText="" actionUrl="" screenReaderActionText="">
            ${crossing.blockReference}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Licence" actionText="" actionUrl="" screenReaderActionText="">
            ${crossing.licenceReference}
        </@fdsCheckAnswers.checkAnswersRow>

        <#if crossing.blockOwnedCompletelyByHolder || crossing.blockOperatorList?has_content>
            <@fdsCheckAnswers.checkAnswersRow keyText="Owner" actionText="" actionUrl="" screenReaderActionText="">
                <#if crossing.blockOwnedCompletelyByHolder>
                  Holder owned
                </#if>
                <#list crossing.blockOperatorList as operator>
                  ${operator}
                </#list>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove block crossing" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to licence and blocks" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>