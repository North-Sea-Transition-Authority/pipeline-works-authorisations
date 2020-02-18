<#include '../../layout.ftl'>
<#import '../../dummyFileUpload.ftl' as dummyFileUpload/>

<#-- @ftlvariable name="medianLineSelections" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="holderCompanyName" type="java.lang.String" -->

<@defaultPage htmlTitle="Location details" pageHeading="Location details">

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup labelText="Will the proposed works cross the median line?" path="form.medianLineSelection" hiddenContent=true fieldsetHeadingClass="govuk-fieldset__legend--l">
            <#list medianLineSelections as name, value>
                <@fdsRadio.radioItem path="form.medianLineSelection" itemMap={name: value}>
                    <#if name != "NOT_CROSSED">
                        <@fdsTextarea.textarea path="form.medianLineAgreement" labelText="Median line agreement" hintText="Please provide the status of your median line agreement"/>
                    </#if>
                    <#if name == "AGREED">
                        <@dummyFileUpload.fileUpload id="1" uploadUrl="/" maxAllowedSize="500" downloadUrl="/" deleteUrl="/" allowedExtensions="txt"/>
                    </#if>
                </@fdsRadio.radioItem>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsRadio.radioGroup path="form.likelySignificantImpact" labelText="Does the development present a significant trans-boundary environmental effect?" hintText="As described in the ESPOO Convention" fieldsetHeadingClass="govuk-fieldset__legend--l">
            <@fdsRadio.radioYes path="form.likelySignificantImpact"/>
            <@fdsRadio.radioNo path="form.likelySignificantImpact"/>
        </@fdsRadio.radioGroup>

        <@fdsFieldset.fieldset legendHeading="Environmental">
          <@fdsDateInput.dateInput formId="1" dayPath="form.emtSubmitByDay" monthPath="form.emtSubmitByMonth" yearPath="form.emtSubmitByYear" labelText="What is the latest you will submit relevant environmental permits to BEIS EMT?"/>
          <@fdsTextarea.textarea path="form.emtStatement" labelText="Actions to be taken to satisfy relevant environmental regulations" hintText="For example, Environmental Statement (ES), Direction and Exemptions"/>
        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Decommissioning">
          <@fdsTextarea.textarea path="form.decommissioningPlans" labelText="What are your decommissioning plans?"/>
          <@fdsCheckbox.checkbox path="form.acceptEolRegulations" labelText="I accept that options for the decommissioning of the pipeline(s) will be considered at the end of the field life and should adhere to Government policies and regulations in force at the time."/>
          <@fdsCheckbox.checkbox path="form.acceptEolRemoval" labelText="I accept that any mattresses or grout bags which have been installed to protect pipelines during their operational life should be removed for disposal onshore."/>
          <@fdsCheckbox.checkbox path="form.acceptRemovalProposal" labelText="I accept that if the condition of the mattresses or grout bags is such that they cannot be removed safely or efficiently then any proposal to leave them in place must be supported by an appropriate comparative assessment of the options."/>
        </@fdsFieldset.fieldset>
    </@fdsForm.htmlForm>

</@defaultPage>