<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkText" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="selectPipelineHintText" type="java.lang.String" -->
<#-- @ftlvariable name="pipelineOptions" type="java.util.Map<Integer, String> -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false >

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>
    <@fdsInsetText.insetText>
      Only pipelines with more than 1 ident can be selected when defining a split.
    </@fdsInsetText.insetText>

    <@fdsForm.htmlForm>
      <@fdsSearchSelector.searchSelectorEnhanced
        path="form.pipelineId"
        options=pipelineOptions
        labelText="On which pipeline do you want to define a split?"
        hintText=selectPipelineHintText
      />

      <@fdsTextInput.textInput
        path="form.numberOfSections"
        suffix="sections"
        labelText="How many sections do you want to split the pipeline into?"
        hintText="Specify 1 section to merge a split pipeline"
        inputClass="govuk-input--width-2"
      />

      <@fdsAction.submitButtons
        primaryButtonText="Continue"
        linkSecondaryAction=true
        secondaryLinkText=backLinkText
        linkSecondaryActionUrl=springUrl(backUrl)
      />

    </@fdsForm.htmlForm>

</@defaultPage>