<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposits" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm>" --> 


<@defaultPage htmlTitle="Deposits" pageHeading="Deposits" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsInsetText.insetText>
            The Consent will only authorise deposits exactly as described, up to the maximum quantities specified to be laid, in the positions listed and within the period stated within the Table - nothing else can be laid.
        </@fdsInsetText.insetText>

        <#list deposits as deposit>
            <@fdsFieldset.fieldset legendHeading=deposit.depositReference />
            <@fdsAction.link  linkText="Change" linkUrl=springUrl(editDepositUrls[deposit.entityID?string.number]) linkClass="govuk-link govuk-link--button" />
            <table class="govuk-table">
                <tbody class="govuk-table__body">                  

                    <#assign size="" quantity="" contingency="" groutBagsDescription=""/>
                    <#if deposit.materialType = "CONCRETE_MATTRESSES">
                       <#assign size= deposit.concreteMattressLength + "m x " + deposit.concreteMattressWidth + "m x " + deposit.concreteMattressDepth + "m"/>
                       <#assign quantity=deposit.quantityConcrete/>
                       <#if deposit.contingencyConcreteAmount??> <#assign contingency=deposit.contingencyConcreteAmount/> </#if>     
                        
                    <#elseif deposit.materialType = "ROCK">
                        <#assign size="Grade " + deposit.rocksSize/>
                        <#assign quantity=deposit.quantityRocks/>
                        <#if deposit.contingencyRocksAmount??> <#assign contingency=deposit.contingencyRocksAmount/> </#if>                  
                        
                    <#elseif deposit.materialType = "GROUT_BAGS">
                        <#assign size=deposit.groutBagsSize + "kg"/>
                        <#assign quantity=deposit.quantityGroutBags/>  
                        <#if deposit.contingencyGroutBagsAmount??> <#assign contingency=deposit.contingencyGroutBagsAmount/> </#if>               
                        <#if deposit.groutBagsBioDegradable?? && deposit.groutBagsBioDegradable == false> <#assign groutBagsDescription=deposit.bioGroutBagsNotUsedDescription/> </#if> 
                        
                    <#elseif deposit.materialType = "OTHER">
                        <#assign size=deposit.otherMaterialSize/>
                        <#assign quantity=deposit.quantityOther/>
                        <#if deposit.contingencyOtherAmount??> <#assign contingency=deposit.contingencyOtherAmount/> </#if>    
                    </#if>

                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Pipeline</th>
                        <td class="govuk-table__cell">
                            <#list deposit.selectedPipelines as pipeline>${pipeline}<#sep>, </#list>
                        </td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Proposed date</th>
                        <td class="govuk-table__cell"> ${deposit.fromMonth} / ${deposit.fromYear}</td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">to date</th>
                        <td class="govuk-table__cell"> ${deposit.toMonth} / ${deposit.toYear}</td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Type of materials</th>
                        <td class="govuk-table__cell"> ${deposit.materialType.getDisplayText()}</td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Size</th>
                        <td class="govuk-table__cell"> ${size} </td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Quantity</th>
                        <td class="govuk-table__cell"> ${quantity}</td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">Contingency Included</th>
                        <td class="govuk-table__cell"> ${contingency}</td>                    
                    </tr>
                    <#if deposit.groutBagsBioDegradable?? && deposit.groutBagsBioDegradable == false>
                        <tr class="govuk-table__row">
                            <th scope="row" class="govuk-table__header">Biodegradeable grout bags</th>
                            <td class="govuk-table__cell"> ${groutBagsDescription}</td>                  
                        </tr>
                    </#if>

                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">From (WGS84)</th>
                        <td class="govuk-table__cell"> 
                            Latitude &nbsp;
                            ${deposit.fromCoordinateForm.latitudeDegrees} ° &nbsp;
                            ${deposit.fromCoordinateForm.latitudeDegrees} ' &nbsp;
                            ${deposit.fromCoordinateForm.latitudeSeconds} " &nbsp;
                            ${deposit.fromCoordinateForm.latitudeDirection}                            
                            <br>
                            Longitude &nbsp;
                            ${deposit.fromCoordinateForm.longitudeDegrees} ° &nbsp;
                            ${deposit.fromCoordinateForm.longitudeMinutes} ' &nbsp;
                            ${deposit.fromCoordinateForm.longitudeSeconds} " &nbsp;
                            ${deposit.fromCoordinateForm.longitudeDirection}    
                        </td>                    
                    </tr>
                    <tr class="govuk-table__row">
                        <th scope="row" class="govuk-table__header">To (WGS84)</th>
                        <td class="govuk-table__cell"> 
                            Latitude &nbsp;
                            ${deposit.toCoordinateForm.latitudeDegrees} ° &nbsp;
                            ${deposit.toCoordinateForm.latitudeDegrees} ' &nbsp;
                            ${deposit.toCoordinateForm.latitudeSeconds} " &nbsp;
                            ${deposit.toCoordinateForm.latitudeDirection}                            
                            <br>
                            Longitude &nbsp;
                            ${deposit.toCoordinateForm.longitudeDegrees} ° &nbsp;
                            ${deposit.toCoordinateForm.longitudeMinutes} ' &nbsp;
                            ${deposit.toCoordinateForm.longitudeSeconds} " &nbsp;
                            ${deposit.toCoordinateForm.longitudeDirection} 
                        </td>                    
                    </tr>                   
                </tbody>
            </table>
        </#list>


       


        <@fdsAction.link linkText="Add deposit" linkUrl=springUrl(addDepositUrl) linkClass="govuk-button govuk-button--blue"/>
        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>