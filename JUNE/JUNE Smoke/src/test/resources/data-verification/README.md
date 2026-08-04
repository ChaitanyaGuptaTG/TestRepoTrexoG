This folder is retained for backward compatibility only.

Use the project-level `source/` folder for expected Excel files.

Naming format:

`<Intent_Name>_Scenario_<Scenario_Number>.xlsx`

Examples:

`source/US_Patent_Scenario_1.xlsx`

`source/US_Trademark_Scenario_3.xlsx`

`source/AU_Patent_Scenario_1.xlsx`

`source/AU_Trademark_Scenario_3.xlsx`

`source/EP_Patent_Scenario_1.xlsx`

Generated files are captured under `output/generated/` and matched with the captured Request ID from each UI scenario.

Data rows are compared using key-based unordered matching. By default, column 1 is used as the primary key:

`workflow.ignoreRowOrder=true`

`workflow.primaryKeyColumn=1`

The primary key can be changed to another 1-based column number or a header name in the config files.
