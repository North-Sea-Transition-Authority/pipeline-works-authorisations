<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="modifyUrl" type="java.lang.String" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->
<#-- @ftlvariable name="endDate" type="java.lang.String" -->

<@defaultPage
  htmlTitle="Project extension"
  pageHeading="Project extension"
  breadcrumbs=false
  errorItems=errorList>
  <@fdsInsetText.insetText>
    <p class="govuk-body">
      The expectation is for a 12 month term.
      You must have approval from the Consents and Authorisations Manager (CAM) for anything beyond 12 months.
      You should also discuss BEIS requirements with them at an early stage.
    </p>
    <p class="govuk-body govuk-">
      Current start date: ${startDate}
    </p>
    <p class="govuk-body">
      Latest completion date: ${endDate}
    </p>
    <p class="govuk-body">
      <a href="${springUrl(modifyUrl)}" class="govuk-link">Click here to change your start or end dates</a>
    </p>
    <p class="govuk-body">
      You will lose any progress on this page by clicking this link.
    </p>
  </@fdsInsetText.insetText>
  <@fdsForm.htmlForm>
      <@fdsFieldset.fieldset
        legendHeadingClass="govuk-fieldset__legend--l"
        legendHeading="Project extension permission"
        caption="Provide the email on which the CAM approved your request"
        hintText="If this has not been approved you should email the CAM at consents@authority.co.uk with the background details and your justification."
        captionClass="govuk-caption-l">
          <@fdsFileUpload.fileUpload
            id="project-extension-upload-file-id"
            path="form.uploadedFileWithDescriptionForms"
            uploadUrl=uploadUrl
            deleteUrl=deleteUrl
            maxAllowedSize=fileuploadMaxUploadSize
            allowedExtensions=imageFileUploadAllowedExtensions
            downloadUrl=downloadUrl
            existingFiles=uploadedFileViewList
            dropzoneText="Drag and drop your documents here"
            multiFile=false/>
      </@fdsFieldset.fieldset>
      <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>
  </@fdsForm.htmlForm>
</@defaultPage>
