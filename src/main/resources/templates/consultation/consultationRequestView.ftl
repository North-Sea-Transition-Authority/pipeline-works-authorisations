<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="consultationRequestViewData" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->
<#-- @ftlvariable name="consultationsUrlFactory" type="uk.co.ogauthority.pwa.service.consultations.ConsultationsUrlFactory" -->

<#macro consultationRequestView consultationRequestViewData applicationReference displayAsHistoricalRequest=false>

    <#if displayAsHistoricalRequest>
        <h3 class="govuk-heading-m"> Requested ${consultationRequestViewData.requestDateDisplay} </h3>
    <#else>
        <h3 class="govuk-heading-m"> ${consultationRequestViewData.consulteeGroupName} </h3>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">
        <#nested/>
        <#if !displayAsHistoricalRequest>
            <@fdsCheckAnswers.checkAnswersRow keyText="Requested" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.requestDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
            ${consultationRequestViewData.status.getDisplayName()}
        </@fdsCheckAnswers.checkAnswersRow>

        <#if consultationRequestViewData.status == "RESPONDED">
            <@fdsCheckAnswers.checkAnswersRow keyText=consultationRequestViewData.status.getDisplayName() actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        <#elseif consultationRequestViewData.status == "WITHDRAWN">
            <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.withdrawnByUser}
            </@fdsCheckAnswers.checkAnswersRow>
            <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn on" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.endTimeStamp}
            </@fdsCheckAnswers.checkAnswersRow>
        <#else>
            <@fdsCheckAnswers.checkAnswersRow keyText="Response due" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.dueDateDisplay}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#list consultationRequestViewData.dataList as responseData>

            <@fdsCheckAnswers.checkAnswersRow keyText=responseData.consultationResponseOptionGroup.responseLabel actionText="" actionUrl="" screenReaderActionText="">
                ${responseData.consultationResponseOption.labelText}
                <#if responseData.consultationResponseOption.getRadioInsetText(applicationReference)?has_content>
                  <p>${responseData.consultationResponseOption.getRadioInsetText(applicationReference)}</p>
                </#if>
            </@fdsCheckAnswers.checkAnswersRow>

            <#if responseData.responseText?has_content>
                <@fdsCheckAnswers.checkAnswersRow keyText=responseData.consultationResponseOption.textAreaViewLabelText actionText="" actionUrl="" screenReaderActionText="">
                    ${responseData.responseText}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

        </#list>

        <#if consultationRequestViewData.responseByPerson?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseByPerson}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <#if consultationRequestViewData.consultationResponseFileViews?has_content>
            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="${consultationRequestViewData.consultationResponseDocumentType.displayName}">
                <@pwaFiles.uploadedFileList downloadUrl=springUrl(consultationRequestViewData.downloadFileUrl) existingFiles=consultationRequestViewData.consultationResponseFileViews blockClass="case-history" />
            </@fdsCheckAnswers.checkAnswersRowNoAction>
        </#if>

    </@fdsCheckAnswers.checkAnswers>

</#macro>

