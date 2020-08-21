<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="consultationRequestViewData" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->
<#-- @ftlvariable name="consultationsUrlFactory" type="uk.co.ogauthority.pwa.service.consultations.ConsultationsUrlFactory" -->


<#macro consultationRequestView consultationRequestViewData>
    <h2 class="govuk-heading-m"> ${consultationRequestViewData.currentRequest.consulteeGroupName} </h2>
    <h3 class="govuk-heading-s"> Requested ${consultationRequestViewData.currentRequest.requestDateDisplay} </h3>

    <#if consultationRequestViewData.currentRequest.canWithdraw>
        <@fdsAction.link linkText="Withdraw consultation" linkUrl=springUrl(consultationsUrlFactory.getWithdrawConsultationUrl(consultationRequestViewData.currentRequest.consultationRequestId)) 
        linkClass="govuk-link" linkScreenReaderText="Withdraw consultation" role=false start=false openInNewTab=true/>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestViewData.currentRequest.status.getDisplayName()}
        </br>
        <#if consultationRequestViewData.currentRequest.status == "RESPONDED">
            ${consultationRequestViewData.currentRequest.responseDateDisplay}
        <#elseif consultationRequestViewData.currentRequest.status == "WITHDRAWN">
            Withdrawn by ${consultationRequestViewData.currentRequest.withdrawnByUser} &nbsp; ${consultationRequestViewData.currentRequest.endTimeStamp}
        <#else>
            Due: ${consultationRequestViewData.currentRequest.dueDateDisplay}
        </#if>
        </@fdsCheckAnswers.checkAnswersRow>


        <#if consultationRequestViewData.currentRequest.responseType?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.currentRequest.responseType.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>        


        <#if consultationRequestViewData.currentRequest.responseRejectionReason?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Rejection reason" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.currentRequest.responseRejectionReason}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>  


        <#if consultationRequestViewData.currentRequest.responseByPerson?has_content >
            <@fdsCheckAnswers.checkAnswersRow keyText="Response by" actionText="" actionUrl="" screenReaderActionText="">
                ${consultationRequestViewData.currentRequest.responseByPerson}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

    </@fdsCheckAnswers.checkAnswers>


</#macro>

