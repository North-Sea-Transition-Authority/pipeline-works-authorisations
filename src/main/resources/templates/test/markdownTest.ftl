<#include '../layout.ftl'>

<@defaultPage htmlTitle="Markdown test" pageHeading="Markdown test" twoThirdsColumn=false topNavigation=false>
  <div class="markdown__container">
    <p class="govuk-body">
      Markdown functionality ensures:
    </p>
    <ul class="govuk-list govuk-list--bullet">
      <li>user understands how to use</li>
      <li>they can preview the end result with markdown applied</li>
      <li>it supports merge data</li>
      <li>need to be able apply govuk classes.</li>
      <li>be able to change the list number format e.g. i) ii) 1, 2, etc</li>
    </ul>
    <p class="govuk-body">
      You can enter standard markdown into the textarea below.
    </p>
    <ul class="govuk-list govuk-list--bullet">
      <li>
        lists will be shown using lowercase alpha character, a. b. c. etc to demonstrate the ability to add custom classes
      </li>
      <li>mail merge data is supported by using ??FORENAME?? or ??SURNAME??</li>
    </ul>
    <div class="markdown__input">
      <@fdsForm.htmlForm>
        <@fdsTextarea.textarea path="form.markdown" labelText="Enter markdown" rows="10" />
        <@fdsDetails.summaryDetails summaryTitle="What is markdown?">
          <p class="govuk-body">
            Some initial guidance. See guidance link below from GovSpeak which we could a similar document for.
          </p>
          <@fdsAction.link linkText="GovSpeak markdown guidance" linkUrl="https://govspeak-preview.herokuapp.com/guide" openInNewTab=true/>
        </@fdsDetails.summaryDetails>
        <@fdsAction.button buttonText="Preview"/>
      </@fdsForm.htmlForm>
    </div>
    <#if html?has_content>
      <div class="markdown__preview">
        <#noautoesc>
          ${html}
        </#noautoesc>
      </div>
    </#if>
  </div>
</@defaultPage>