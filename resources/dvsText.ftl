<#ftl strip_text=true strip_whitespace=true>
<@compress single_line=true>
<b>Mapping details:</b> This map shows the predicted probability of occurrence 
of three dominant <i>Anopheles</i> vectors of human malaria in ${areaName}. The constituent 
probability of occurrence maps for each species were created with the Boosted Regression Trees (BRT) 
technique as described in Sinka <i>et al.</i> (2010). The dominant <i>Anopheles</i> vectors of human 
malaria in the Americas: occurrence data, distribution maps and bionomic précis. <i>Parasites and Vectors</i>, <b>3</b>:72.
<br><b>Interpretation:</b> The map takes advantage of the red green blue (RGB) additive colour model 
mapped to a cube in the legend. The PO values &lt; 0.5 were first set to zero and those &gt;= 0.5 to 1.0 
stretched across the RGB colour range. The horizontal x-axis has green values (PO of ${anos[2].getScientificAbbreviation()}) 
increasing to the left, the y-axis has blue values (PO of ${anos[1].getScientificAbbreviation()}) increasing to the lower right 
and the vertical z-axis has red values (PO of ${anos[0].getScientificAbbreviation()}) increasing towards the top. Thus areas with a 
PO of one for a single species and zero for the other two will display as a primary colour. A PO of one 
for all three species will show white and PO of &lt; 0.5 for all three species black. The full colour gamut 
of the RGB cube is impossible to show in two dimensions; the black origin for example, is the hidden back of the cube.
<br><#include "copyright.ftl">
</@compress>