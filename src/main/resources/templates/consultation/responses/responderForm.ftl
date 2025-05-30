<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="responseOptionGroupMap" type="java.util.List<uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption>" -->
<#-- @ftlvariable name="appRef" type="String" -->
<#-- @ftlvariable name="previousResponses" type="java.util.List<uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="consulteeGroupName" type="String" -->

<#include '../../layout.ftl'>
<#include '../consultationRequestView.ftl'>

<@defaultPage htmlTitle="${appRef} consultation response" topNavigation=true fullWidthColumn=true breadcrumbs=true>

  <#assign rejectAppSummaryDetails>
    <@fdsDetails.summaryDetails summaryTitle="How do I request changes to specific questions on the application?">
      <p>State the question(s) and what you want updated in the rejection reason.
        Provide the name of the question, application section, pipeline number, ident number, deposit name, schematic/drawing reference, etc as appropriate to ensure the applicant knows which question is being referred to.
        Some examples are provided below:</p>

      <ul class="govuk-list govuk-list--bullet">
        <li>provide xxx about the insulation / coating type for ident 5 on PL1234 </li>
        <li>provide the reference number of your submitted environmental permit in the environmental section </li>
        <li>correct the pipeline schematic reference xxxxx as it doesn’t show the pipeline it is linked to </li>
      </ul>
    </@fdsDetails.summaryDetails>
  </#assign>

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

        <#list responseOptionGroupMap as responseOptionGroup, responseOptions>

          <@fdsRadio.radioGroup path="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" labelText=responseOptionGroup.questionText hiddenContent=true fieldsetHeadingSize="h3">
            <#assign firstItem=true/>
            <#list responseOptions as responseOption>
              <@fdsRadio.radioItem path="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" itemMap={responseOption : responseOption.getLabelText()} isFirstItem=firstItem>

                <#if responseOption.getRadioInsetText(appRef)?has_content>
                  <@fdsInsetText.insetText>
                    ${responseOption.getRadioInsetText(appRef)}
                  </@fdsInsetText.insetText>
                </#if>

                <#if responseOption == "CONFIRMED" || responseOption == "EIA_AGREE" || responseOption == "HABITATS_AGREE">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option1Description" labelText=responseOption.textAreaLabelText optionalLabel=true nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </#if>
                <#if responseOption == "REJECTED">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option2Description" labelText=responseOption.textAreaLabelText nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" characterCount=true maxCharacterLength=maxCharacterLength?c/>

                  ${rejectAppSummaryDetails}
                </#if>
                <#if responseOption == "PROVIDE_ADVICE">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option1Description" labelText=responseOption.textAreaLabelText nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </#if>
                <#if responseOption == "NO_ADVICE">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option2Description" labelText=responseOption.textAreaLabelText nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" optionalLabel=true characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </#if>
                <#if responseOption == "EIA_DISAGREE" || responseOption == "HABITATS_DISAGREE">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option2Description" labelText=responseOption.textAreaLabelText nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                  ${rejectAppSummaryDetails}
                </#if>
                <#if responseOption == "EIA_NOT_RELEVANT" || responseOption == "HABITATS_NOT_RELEVANT">
                  <@fdsTextarea.textarea path="form.responseDataForms[${responseOptionGroup}].option3Description" labelText=responseOption.textAreaLabelText nestingPath="form.responseDataForms[${responseOptionGroup}].consultationResponseOption" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </#if>
              </@fdsRadio.radioItem>
              <#assign firstItem=false/>
            </#list>
          </@fdsRadio.radioGroup>

        </#list>

        <@fdsFieldset.fieldset legendHeading="${consultationResponseDocumentType.questionText}" legendHeadingClass="govuk-fieldset__legend--s" legendHeadingSize="h3" hintText="${consultationResponseDocumentType.questionGuidance}">
          <@fdsFileUpload.fileUpload
            id="fileUpload"
            path="form.uploadedFiles"
            uploadUrl=fileUploadAttributes.uploadUrl()
            downloadUrl=fileUploadAttributes.downloadUrl()
            deleteUrl=fileUploadAttributes.deleteUrl()
            maxAllowedSize=fileUploadAttributes.maxAllowedSize()
            allowedExtensions=fileUploadAttributes.allowedExtensions()
            existingFiles=fileUploadAttributes.existingFiles()
            dropzoneText="Drag and drop your documents here"
          />
        </@fdsFieldset.fieldset>

        <#if previousResponses?has_content>
          <@fdsDetails.summaryDetails summaryTitle="Show my previous advice">

            <#list previousResponses as previousResponse>
                <@consultationRequestView consultationRequestViewData=previousResponse applicationReference=appRef/>
            </#list>

          </@fdsDetails.summaryDetails>
        </#if>

        <@fdsAction.submitButtons primaryButtonText="Submit response" linkSecondaryAction=true secondaryLinkText="Back to tasks" linkSecondaryActionUrl=springUrl(cancelUrl)/>

      </@fdsForm.htmlForm>

    </@grid.twoThirdsColumn>
  </@grid.gridRow>

</@defaultPage>