<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="optionsTemplateFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView" -->

<div class="pwa-application-summary-section">

  <h2 class="govuk-heading-l" id="optionsTemplate">${sectionDisplayText}</h2>

    <#if optionsTemplateFileView?has_content>

      <@fdsCheckAnswers.checkAnswers summaryListClass="">

        <div class="govuk-summary-list__row">
          <dt class="govuk-summary-list__key">
              <@fdsAction.link linkText=optionsTemplateFileView.fileName linkUrl=springUrl(optionsTemplateFileView.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${optionsTemplateFileView.fileName}" role=false start=false openInNewTab=true/>
          </dt>
          <dd class="govuk-summary-list__value">
              <@multiLineText.multiLineText blockClass="govuk-summary-list">${optionsTemplateFileView.fileDescription!}</@multiLineText.multiLineText>
          </dd>
        </div>

      </@fdsCheckAnswers.checkAnswers>

      <#else>

        <@fdsInsetText.insetText>No options template has been uploaded.</@fdsInsetText.insetText>

    </#if>

</div>