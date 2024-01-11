<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="crossing" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingView" -->

<@defaultPage htmlTitle="Remove carbon storage area crossing" pageHeading="Are you sure you want to remove this area crossing?" breadcrumbs=true errorItems=errorList>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <@fdsCheckAnswers.checkAnswersRow keyText="Carbon storage area reference" actionText="" actionUrl="" screenReaderActionText="">
            ${crossing.storageAreaReference}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Owner" actionText="" actionUrl="" screenReaderActionText="">
            <#if crossing.ownedCompletelyByHolder>
              Holder owned
            </#if>
            <#list crossing.operatorList as operator>
                ${operator}
            </#list>
        </@fdsCheckAnswers.checkAnswersRow>

    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove storage area crossing" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to carbon storage areas" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>
