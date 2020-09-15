<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="docFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->

<div class="pwa-application-summary-section">

  <h2 class="govuk-heading-l" id="supplementaryDocuments">${sectionDisplayText}</h2>

    <#if docFileViews?has_content>

        <@fdsCheckAnswers.checkAnswers summaryListClass="">

            <#list docFileViews as fileView>

              <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">
                    <@fdsAction.link linkText=fileView.fileName linkUrl=springUrl(fileView.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${fileView.fileName}" role=false start=false openInNewTab=true/>
                </dt>
                <dd class="govuk-summary-list__value">
                    <@multiLineText.multiLineText blockClass="govuk-summary-list">${fileView.fileDescription!}</@multiLineText.multiLineText>
                </dd>
              </div>

            </#list>

        </@fdsCheckAnswers.checkAnswers>

        <#else>

          <@fdsInsetText.insetText>No supplementary documents have been uploaded.</@fdsInsetText.insetText>

    </#if>

</div>