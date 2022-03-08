<#-- (c) https://github.com/MontiCore/monticore -->
<#--
  Constructor for the main class of a SC

-->
${tc.signature("classname", "states", "initialState")}
${cd4c.constructor("public ${classname}()")}
{
  ${glex.defineHookPoint(tc,"<JavaBlock>?StateInitConstructorV2:begin",ast, classname, states, initialState)}
  <#list states as state>
    this.${state?uncap_first} = new ${classname}_${state}();
  </#list>
  <#if initialState??>
    this.state = this.${initialState?uncap_first};
  </#if>
  ${glex.defineHookPoint(tc,"<JavaBlock>?StateInitConstructorV2:begin",ast, classname, states, initialState)}
}
