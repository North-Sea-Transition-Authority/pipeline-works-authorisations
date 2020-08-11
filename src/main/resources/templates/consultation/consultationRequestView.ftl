<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="consultationRequestViewData" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->


<#macro consultationRequestView consultationRequestViewData>
    <h2 class="govuk-heading-m"> ${consultationRequestViewData.currentRequest.consulteeGroupName} </h2>
    <h3 class="govuk-heading-s"> Requested ${consultationRequestViewData.currentRequest.requestDateDisplay} </h3>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestViewData.currentRequest.status.getDisplayName()}
        </br>
        Due: ${consultationRequestViewData.currentRequest.dueDateDisplay}
        </@fdsCheckAnswers.checkAnswersRow>

        <#if consultationRequestViewData.currentRequest.responseType?? >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.currentRequest.responseType.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>        

        <#if consultationRequestViewData.currentRequest.responseByPerson?? >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.currentRequest.responseByPerson}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

    </@fdsCheckAnswers.checkAnswers>


</#macro>

