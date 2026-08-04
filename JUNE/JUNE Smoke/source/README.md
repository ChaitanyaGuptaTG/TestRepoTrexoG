Place expected source Excel files in this folder.

Naming format:

<Intent_Name>_Scenario_<Scenario_Number>.xlsx

Examples:

US_Patent_Scenario_1.xlsx
US_Patent_Scenario_3.xlsx
EP_Patent_Scenario_1.xlsx
EP_Patent_Scenario_3.xlsx
AU_Trademark_Scenario_1.xlsx

Generated files are captured under ../output/generated and matched by the Request ID captured during the UI scenario.

Rows are compared without depending on row order. The default primary key is column 1, controlled by:

workflow.ignoreRowOrder=true
workflow.primaryKeyColumn=1

`workflow.primaryKeyColumn` can also be set to a header name when a different matching column is needed.
