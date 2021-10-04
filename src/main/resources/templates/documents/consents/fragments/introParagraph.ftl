<#-- @ftlvariable name="paragraphText" type="String"-->

<#macro paragraph paragraphText="">
  <div class="pwa-intro-paragraph">
    ${paragraphText?no_esc}
  </div>
</#macro>