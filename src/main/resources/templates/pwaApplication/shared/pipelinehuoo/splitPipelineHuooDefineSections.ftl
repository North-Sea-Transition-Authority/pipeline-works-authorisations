<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkText" type="java.lang.String" -->
<#-- @ftlvariable name="submitButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="pickableIdentOptions" type="java.util.Map<uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.DefinePipelineHuooSectionsForm" -->
<#-- @ftlvariable name="firstSectionStartDescription" type="java.lang.String" -->
<#-- @ftlvariable name="lastSectionEndDescription" type="java.lang.String" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=false fullWidthColumn=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
  </#if>

    <#-- title is long due to using the pipeline name, use two thirds width for everything else   -->
  <div class="govuk-width-container">
    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <@fdsForm.htmlForm>

          <#list 0..(totalSections-1) as sectionNumber>
            <@sectionStartPointOptions
            listPath="form.pipelineSectionPoints"
            pickableIdentOptions=pickableIdentOptions
            sectionNumberIndex=sectionNumber
            isLastSection=sectionNumber?is_last
            isFirstSection=sectionNumber==0/>
          </#list>

          <@fdsAction.submitButtons
            primaryButtonText="Confirm pipeline sections"
            linkSecondaryAction=true
            secondaryLinkText=backLinkText
            linkSecondaryActionUrl=springUrl(backUrl)
            />

        </@fdsForm.htmlForm>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>
  </div>

</@defaultPage>

<#macro sectionStartPointOptions listPath pickableIdentOptions sectionNumberIndex isLastSection isFirstSection>
  <#local identLocationOptionPath=createNestedFormPath(listPath, sectionNumberIndex, "pickedPipelineIdentString") />
  <#local identLocationIncludedInSectionPath=createNestedFormPath(listPath, sectionNumberIndex, "pointIncludedInSection") />
  <h2 class="govuk-heading-m">Section ${sectionNumberIndex+1}</h2>

    <#if isFirstSection>
        <@fdsInsetText.insetText>${firstSectionStartDescription}</@fdsInsetText.insetText>
        <@hiddenInput path=identLocationOptionPath />
        <@hiddenInput path=identLocationIncludedInSectionPath />
    <#else>
        <@fdsSearchSelector.searchSelectorEnhanced path=identLocationOptionPath labelText="Where does the section start?" options=pickableIdentOptions />
        <@fdsRadio.radioGroup path=identLocationIncludedInSectionPath
        labelText="Is the selected point located in this section?"
        hintText="Answer 'Yes' if the previous section does not include this point"
        fieldsetHeadingClass="govuk-fieldset__legend--s"
        fieldsetHeadingSize="h3"
        hiddenContent=true>
            <@fdsRadio.radioYes path=identLocationIncludedInSectionPath/>
            <@fdsRadio.radioNo path=identLocationIncludedInSectionPath/>
        </@fdsRadio.radioGroup>
    </#if>

    <#if isLastSection>
        <@fdsInsetText.insetText>${lastSectionEndDescription}</@fdsInsetText.insetText>
    </#if>



</#macro>

<#macro hiddenInput path>
    <@spring.bind path/>
    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#local name=spring.status.expression>
    <#local value=spring.stringStatusValue>

    <input type="hidden" id="${id}" name="${name}" value="${value}">
</#macro>
