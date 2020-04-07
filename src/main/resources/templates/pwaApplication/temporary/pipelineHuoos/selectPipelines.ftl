<#include '../../../layout.ftl'>

<#-- @ftlvariable name="selectHuooCategoryUrl" type="String" -->
<#-- @ftlvariable name="multipleSelection" type="boolean" -->
<#-- @ftlvariable name="multipleSelectionUrl" type="String" -->
<#-- @ftlvariable name="cancelSelectionUrl" type="String" -->
<#-- @ftlvariable name="pipes" type="java.util.List<uk.co.ogauthority.pwa.temp.model.view.SelectPipelineView>" -->

<@defaultPage htmlTitle="Select pipelines to manage organisations" pageHeading="Select pipelines to manage organisations" breadcrumbs=true fullWidthColumn=true>

    <#if !multipleSelection>
        <div class="govuk-list govuk-list--inline">
          <div class="govuk-list__item govuk-list__item--inline">
              <@fdsAction.link linkText="Select multiple" linkUrl=springUrl(multipleSelectionUrl) linkClass="govuk-link" />
          </div>
        </div>
      <#else>
        <div class="govuk-list govuk-list--inline">
          <div class="govuk-list__item govuk-list__item--inline">
            <@fdsAction.link linkText="Select all" linkUrl="#" linkClass="govuk-link" />
          </div>
          <div class="govuk-list__item govuk-list__item--inline">
            <@fdsAction.link linkText="Select none" linkUrl="#" linkClass="govuk-link" />
          </div>
          <div class="govuk-list__item govuk-list__item--inline">
            <@fdsAction.link linkText="Cancel selection" linkUrl=springUrl(cancelSelectionUrl) linkClass="govuk-link" />
          </div>
        </div>
    </#if>

    <table class="govuk-table">
      <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <#if multipleSelection>
            <th class="govuk-table__header" scope="col">Select</th>
          </#if>
          <th class="govuk-table__header" scope="col">Pipeline number</th>
          <th class="govuk-table__header" scope="col">Ident number</th>
          <th class="govuk-table__header" scope="col">Holders</th>
          <th class="govuk-table__header" scope="col">Users</th>
          <th class="govuk-table__header" scope="col">Operators</th>
          <th class="govuk-table__header" scope="col">Owners</th>
          <#if !multipleSelection>
            <th class="govuk-table__header" scope="col">Actions</th>
          </#if>
        </tr>
      </thead>
      <tbody class="govuk-table__body">
        <#list pipes as pipe>
            <#list 0..6 as i>
              <tr class="govuk-table__row">
                  <#if multipleSelection>
                    <td class="govuk-table__cell">
                        <@fdsCheckbox.checkbox path="form.select" labelText="" />
                    </td>
                  </#if>
                <td class="govuk-table__cell">${pipe.number}</td>
                <td class="govuk-table__cell">#{pipe.identNumber + i}</td>
                <td class="govuk-table__cell">
                    <#list pipe.holders as holder>
                        ${holder}
                      <br/>
                    </#list>
                </td>
                <td class="govuk-table__cell">
                    <#list pipe.users as user>
                        ${user}
                      <br/>
                    </#list>
                </td>
                <td class="govuk-table__cell">
                    <#list pipe.operators as operator>
                        ${operator}
                      <br/>
                    </#list>
                </td>
                <td class="govuk-table__cell">
                    <#list pipe.owners as owner>
                        ${owner}
                      <br/>
                    </#list>
                </td>
                  <#if !multipleSelection>
                    <td class="govuk-table__cell">
                        <@fdsForm.htmlForm>
                            <@fdsAction.button buttonText="Edit" buttonClass="fds-link-button"/>
                        </@fdsForm.htmlForm>
                    </td>
                  </#if>
              </tr>
            </#list>
        </#list>
      </tbody>
    </table>

    <#if multipleSelection>
      <@fdsForm.htmlForm>
        <@fdsAction.button buttonText="Continue" />
      </@fdsForm.htmlForm>
    </#if>

</@defaultPage>