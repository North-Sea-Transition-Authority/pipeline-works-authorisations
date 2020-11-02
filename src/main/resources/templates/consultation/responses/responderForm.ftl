<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="responseOptions" type="java.util.List<uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption>" -->
<#-- @ftlvariable name="appRef" type="String" -->
<#-- @ftlvariable name="previousResponses" type="java.util.List<uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="consulteeGroupName" type="String" -->

<#include '../../layout.ftl'>
<#include '../consultationRequestView.ftl'>

<@defaultPage htmlTitle="${appRef} consultation response" topNavigation=true fullWidthColumn=true breadcrumbs=true>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
      </#if>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <@grid.gridRow>
    <@grid.fullColumn>
      <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />
    </@grid.fullColumn>
  </@grid.gridRow>

  <@grid.gridRow>
    <@grid.twoThirdsColumn>

      <span class="govuk-caption-m">${consulteeGroupName}</span>
      <h2 class="govuk-heading-l">Consultation response</h2>

      <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.consultationResponseOption" labelText="What is your response on this application?" hiddenContent=true>
          <#assign firstItem=true/>
          <#list responseOptions as  responseOption>
            <@fdsRadio.radioItem path="form.consultationResponseOption" itemMap={responseOption : responseOption.getDisplayText()} isFirstItem=firstItem>
              <#if responseOption == "CONFIRMED">
                <@fdsTextarea.textarea path="form.confirmedDescription" labelText="Provide consent conditions if they apply (optional)" nestingPath="form.consultationResponseOption" characterCount=true maxCharacterLength="4000"/>
              </#if>
              <#if responseOption == "REJECTED">
                <@fdsTextarea.textarea path="form.rejectedDescription" labelText="Why are you rejecting this application?" nestingPath="form.consultationResponseOption" characterCount=true maxCharacterLength="4000"/>

                <@fdsDetails.summaryDetails summaryTitle="How do I request changes to specific questions on the application?">
                  <p>State the question(s) and what you want updated in the rejection reason.
                    Provide the name of the question, application section, pipeline number, ident number, deposit name, schematic/drawing reference, etc as appropriate to ensure the applicant knows which question is being referred to.
                    Some examples are provided below:</p>

                  <ul>
                    <li>provide xxx about the insulation / coating type for ident 5 on PL1234 </li>
                    <li>provide the reference number of your submitted environmental permit in the environmental section </li>
                    <li>correct the pipeline schematic reference xxxxx as it doesn’t show the pipeline it is linked to </li>
                  </ul>
                </@fdsDetails.summaryDetails>
              </#if>
            </@fdsRadio.radioItem>
            <#assign firstItem=false/>
          </#list>
        </@fdsRadio.radioGroup>

        <#if previousResponses?has_content>
          <@fdsDetails.summaryDetails summaryTitle="Show my previous advice">

            <#list previousResponses as previousResponse>
              <@consultationRequestView previousResponse/>
            </#list>

          </@fdsDetails.summaryDetails>
        </#if>

        <@fdsAction.submitButtons primaryButtonText="Submit response" linkSecondaryAction=true secondaryLinkText="Back to tasks" linkSecondaryActionUrl=springUrl(cancelUrl)/>

      </@fdsForm.htmlForm>

    </@grid.twoThirdsColumn>
  </@grid.gridRow>

</@defaultPage>