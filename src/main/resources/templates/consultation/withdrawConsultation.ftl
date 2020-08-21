<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="consultationRequestView" type="uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Withdraw" pageHeading="Are you sure you want to withdraw this consultation?" topNavigation=true>

  <@fdsForm.htmlForm>
    <@fdsCheckAnswers.checkAnswers summaryListClass="">

      <@fdsCheckAnswers.checkAnswersRow keyText="Consultee" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestView.consulteeGroupName}
      </@fdsCheckAnswers.checkAnswersRow>
      
      <@fdsCheckAnswers.checkAnswersRow keyText="Requested" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestView.requestDateDisplay}
      </@fdsCheckAnswers.checkAnswersRow>

      <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
        ${consultationRequestView.status.getDisplayName()}
        </br>
        Due: ${consultationRequestView.dueDateDisplay}
      </@fdsCheckAnswers.checkAnswersRow>

    </@fdsCheckAnswers.checkAnswers>


    <@fdsAction.submitButtons primaryButtonText="Withdraw" linkSecondaryAction=true secondaryLinkText="Do not withdraw" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>

</@defaultPage>