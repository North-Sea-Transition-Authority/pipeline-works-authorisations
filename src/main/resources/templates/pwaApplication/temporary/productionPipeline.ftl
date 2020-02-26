<#include '../../layout.ftl'>
<#import '../../dummyFileUpload.ftl' as dummyFileUpload/>

<#-- @ftlvariable name="pipelineView" type="uk.co.ogauthority.pwa.temp.model.view.PipelineView" -->
<#-- @ftlvariable name="addIdentUrl" type="String" -->
<#-- @ftlvariable name="backToPipelinesUrl" type="String" -->

<#assign heading = "${pipelineView.pipelineType.displayName} - ${pipelineView.pipelineNumber}" />

<@defaultPage htmlTitle=heading pageHeading=heading twoThirdsColumn=false breadcrumbs=true>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValuesNumber key="Length" value="${pipelineView.length}m" valueId="${pipelineView.pipelineNumber}-length" />
        <@fdsDataItems.dataValues key="From" value="${pipelineView.from} ${pipelineView.getFromLatString()?no_esc} ${pipelineView.getFromLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="To" value="${pipelineView.to} ${pipelineView.getToLatString()?no_esc} ${pipelineView.getToLongString()?no_esc}" />
        <@fdsDataItems.dataValues key="Component parts" value=pipelineView.componentParts!"" />
        <@fdsDataItems.dataValues key="Products to be conveyed" value=pipelineView.productsToBeConveyed!"" />
    </@fdsDataItems.dataItem>

    <#if pipelineView.idents?size gt 0>

        <hr class="govuk-section-break govuk-section-break--l">

        <h3 class="govuk-heading-m">Idents</h3>

        <table class="govuk-table">
            <tbody class="govuk-table__body">
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Ident no.</th>
                    <#list pipelineView.idents as ident>
                        <th class="govuk-table__header govuk-table__header">${ident.identNo!""}</th>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">From</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell">${ident.from!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">To</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell">${ident.to!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Component parts</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell">${ident.componentParts!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Type of insulation/coating</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell">${ident.typeOfInsulationOrCoating!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Products to be conveyed</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell">${ident.productsToBeConveyed!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Length (m)</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell govuk-table__cell">${ident.length!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">External diameter (mm)</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell govuk-table__cell">${ident.externalDiameter!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Internal diameter (mm)</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell govuk-table__cell">${ident.internalDiameter!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Wall thickness (mm)</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell govuk-table__cell">${ident.wallThickness!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">MAOP (Barg)</th>
                    <#list pipelineView.idents as ident>
                        <td class="govuk-table__cell govuk-table__cell">${ident.maop!""}</td>
                    </#list>
                </tr>
                <tr class="govuk-table__row">
                    <th class="govuk-table__header" scope="row">Actions</th>
                    <#list pipelineView.idents as ident>
                      <td class="govuk-table__cell">
                          <@fdsAction.link linkUrl="#" linkText="Edit" />
                          <hr class="govuk-section-break" />
                          <@fdsAction.link linkUrl="#" linkText="Remove" />
                      </td>
                    </#list>
                </tr>
            </tbody>
        </table>

    </#if>

    <@fdsAction.link linkText="Add ident" linkClass="govuk-button govuk-button--secondary" linkUrl=springUrl(addIdentUrl) />
    <@fdsAction.link linkText="Back to pipelines" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(backToPipelinesUrl) />
    <hr class="govuk-section-break govuk-section-break--l"/>

    <@fdsForm.htmlForm>
        <h2 class="govuk-heading-l">Technical Drawing</h2>
        <@dummyFileUpload.fileUpload id="pipelineDrawing" uploadUrl="/" deleteUrl="/" downloadUrl="/" maxAllowedSize=500 allowedExtensions="txt"/>
        <@fdsAction.button buttonText="Back to pipelines" buttonClass="govuk-button"/>
    </@fdsForm.htmlForm>

</@defaultPage>