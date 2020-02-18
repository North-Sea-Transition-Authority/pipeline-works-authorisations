<#include '../../../layout.ftl'>

<#-- @ftlvariable name="crossingTypes" type="java.util.Map<String, String>" -->

<@defaultPage htmlTitle="Add crossing agreement" pageHeading="Add a new crossing agreement">

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.crossingType" labelText="Which crossing agreement is being added?" hiddenContent=true fieldsetHeadingClass="govuk-fieldset__legend--l">
          <#list crossingTypes as name, value>
            <@fdsRadio.radioItem path="form.crossingType" itemMap={name: value}>
              <#if name == "BLOCK">
                <@fdsTextInput.textInput path="form.blockNumber" labelText="UK block number"/>
                <@fdsNumberInput.numberInputItem path="form.licenseNumber" labelText="License number"/>
              <#elseif name == "TELECOMMUNICATION">
                <@fdsTextInput.textInput path="form.cableNameOrLocation" labelText="Cable name/location"/>
                <@fdsTextInput.textInput path="form.holderOfCable" labelText="Holder of cable"/>
              <#elseif name == "PIPELINE">
                <@fdsTextInput.textInput path="form.pipelineNumber" labelText="Pipeline number"/>
                <@fdsTextInput.textInput path="form.ownerOfPipeline" labelText="Owner of pipeline"/>
              </#if>
            </@fdsRadio.radioItem>
          </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.button buttonText="Add agreement"/>

    </@fdsForm.htmlForm>

</@defaultPage>