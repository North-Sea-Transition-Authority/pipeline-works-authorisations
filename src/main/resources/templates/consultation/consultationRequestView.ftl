<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="consultationRequestViewData" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->
<#-- @ftlvariable name="consultationsUrlFactory" type="uk.co.ogauthority.pwa.service.consultations.ConsultationsUrlFactory" -->


<#macro consultationRequestView consultationRequestViewData>

    <h3 class="govuk-heading-m"> ${consultationRequestViewData.consulteeGroupName} </h3>
    <h4 class="govuk-heading-s"> Requested ${consultationRequestViewData.requestDateDisplay} </h4>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">
        <#nested/>
        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestViewData.status.getDisplayName()}
        </br>
        <#if consultationRequestViewData.status == "RESPONDED">
            ${consultationRequestViewData.responseDateDisplay}
        <#elseif consultationRequestViewData.status == "WITHDRAWN">
            Withdrawn by ${consultationRequestViewData.withdrawnByUser} &nbsp; ${consultationRequestViewData.endTimeStamp}
        <#else>
            Due: ${consultationRequestViewData.dueDateDisplay}
        </#if>
        </@fdsCheckAnswers.checkAnswersRow>


        <#if consultationRequestViewData.responseType?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseType.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


        <#if consultationRequestViewData.responseRejectionReason?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Rejection reason" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseRejectionReason}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


        <#if consultationRequestViewData.responseByPerson?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.responseByPerson}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>


    </@fdsCheckAnswers.checkAnswers>


</#macro>

