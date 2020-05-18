<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelines" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="materialTypes" type="java.util.List<MaterialType>" -->
<#-- @ftlvariable name="longDirections" type="java.util.List<LongitudeDirection>" --> 
<#-- @ftlvariable name="proposedStartDate" type="java.lang.String" --> 

<@defaultPage htmlTitle="Permanent Deposits" pageHeading="Permanent Deposits" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.selectedPipelines" options=pipelines labelText="Select Pipelines" multiSelect=true/>
        <@fdsInsetText.insetText>
            Only add more than one pipeline on the same deposit if they’re within the same trench or piggy-backed.
        </@fdsInsetText.insetText>

        <@fdsNumberInput.twoNumberInputs pathOne="form.fromMonth" pathTwo="form.fromYear" labelText="Month and year of deposit start (must be on/after proposed start date of " + proposedStartDate + ")" formId="from-month-year">
            <@fdsNumberInput.numberInputItem path="form.fromMonth" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.fromYear" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>

        <@fdsNumberInput.twoNumberInputs pathOne="form.toMonth" pathTwo="form.toYear" labelText="Month and year of deposit end" formId="to-month-year">
            <@fdsNumberInput.numberInputItem path="form.toMonth" labelText="Month" inputClass="govuk-input--width-2"/>
            <@fdsNumberInput.numberInputItem path="form.toYear" labelText="Year" inputClass="govuk-input--width-4"/>
        </@fdsNumberInput.twoNumberInputs>


        <@fdsRadio.radioGroup path="form.materialType" labelText="Select the material type to be used" hiddenContent=true>                
            <#assign firstItem=true/>
            <#assign contingencyGuidance = "Contingency should be included if appropriate as if more is required you will have to request a new consent"/>
            <#list materialTypes as  materialTypeOption>
                <@fdsRadio.radioItem path="form.materialType" itemMap={materialTypeOption : materialTypeOption.getDisplayText()} isFirstItem=firstItem>   
                    <#if materialTypeOption == "CONCRETE_MATTRESSES">
                        <@fdsTextInput.textInput path="form.concreteMattressLength" labelText="Length: m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressWidth" labelText="Width: m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.concreteMattressDepth" labelText="Depth: m" inputClass="govuk-input--width-10"/>
                        <@fdsTextInput.textInput path="form.quantityConcrete" labelText="Enter quantity of material to be used?"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyConcreteAmount" labelText="How much contingency was included?"/>

                    <#elseif materialTypeOption == "ROCK">
                        <@fdsTextInput.textInput path="form.rocksSize" labelText="Size: Grade e.g (1-5)"/>
                        <@fdsTextInput.textInput path="form.quantityRocks" labelText="Enter quantity of material to be used? (decimal tonnes)"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyRocksAmount" labelText="How much contingency was included?"/>

                    <#elseif materialTypeOption == "GROUT_BAGS">
                        <@fdsTextInput.textInput path="form.groutBagsSize" labelText="Size: kg"/>
                        <@fdsRadio.radioGroup path="form.groutBagsBioDegradable" labelText="Are the grout bags bio-degradable?" hiddenContent=true>  
                            <@fdsRadio.radioYes path="form.groutBagsBioDegradable"/>                                
                            <@fdsRadio.radioNo path="form.groutBagsBioDegradable">
                                <@fdsTextInput.textInput path="form.bioGroutBagsNotUsedDescription" labelText="Why are bio-degradable grout bags not being used?"/>
                            </@fdsRadio.radioNo>                        
                        </@fdsRadio.radioGroup>
                        <@fdsTextInput.textInput path="form.quantityGroutBags" labelText="Enter quantity of material to be used?"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyGroutBagsAmount" labelText="How much contingency was included?"/>

                    <#elseif materialTypeOption == "OTHER">
                        <@fdsTextInput.textInput path="form.otherMaterialSize" labelText="Size: "/>
                        <@fdsTextInput.textInput path="form.quantityOther" labelText="Enter quantity of material to be used?"/>
                        <@fdsInsetText.insetText> ${contingencyGuidance} </@fdsInsetText.insetText>
                        <@fdsTextInput.textInput path="form.contingencyOtherAmount" labelText="How much contingency was included?"/>
                    </#if>
                </@fdsRadio.radioItem>
            <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>


        <@fdsFieldset.fieldset legendHeading="Where is the start location?">
            <@pwaLocationInput.locationInput degreesLocationPath="form.fromLatitudeDegrees"
                                          minutesLocationPath="form.fromLatitudeMinutes"
                                          secondsLocationPath="form.fromLatitudeSeconds"
                                          formId="fromLatitude"
                                          labelText="Start point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromLongitudeDegrees"
                                          minutesLocationPath="form.fromLongitudeMinutes"
                                          secondsLocationPath="form.fromLongitudeSeconds"
                                          direction="EW"
                                          directionPath="form.fromLongitudeDirection"
                                          directionList=longDirections
                                          formId="fromLongitude"
                                          labelText="Start point longitude"/>
        </@fdsFieldset.fieldset> 

        <@fdsFieldset.fieldset legendHeading="Where is the end location?">
            <@pwaLocationInput.locationInput degreesLocationPath="form.toLatitudeDegrees" 
                                          minutesLocationPath="form.toLatitudeMinutes" 
                                          secondsLocationPath="form.toLatitudeSeconds" 
                                          formId="toLatitude" 
                                          labelText="Finish point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.toLongitudeDegrees"
                                          minutesLocationPath="form.toLongitudeMinutes"
                                          secondsLocationPath="form.toLongitudeSeconds"
                                          direction="EW"
                                          directionPath="form.toLongitudeDirection"
                                          directionList=longDirections
                                          formId="toLongitude"
                                          labelText="Finish point longitude"/>
        </@fdsFieldset.fieldset>

        


        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>