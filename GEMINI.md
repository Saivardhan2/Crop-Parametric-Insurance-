# Agent & IDE Operational Rules

## 1. Direct Disk Writes Only (Avoid Diff-Review Reverts)
- *Do NOT use IDE diff-staging tools (write_to_file / replace_file_content) when editing or creating project files.* The IDE's pending Review buffer can automatically revert unaccepted changes at the end of a turn.
- *Direct Disk Writes*: Always write and patch project files directly to disk via Python or PowerShell inside run_command (using UTF-8 encoding). Changes persist immediately on the filesystem without triggering the Review rollback glitch.
- Note: Artifacts in the agent brain directory (brain/<conversation-id>/...) are exempt and follow standard artifact creation.

## 2. Windows PowerShell Constraints
- *Directory Changes*: Never issue cd commands in run_command; always set the execution folder using the Cwd parameter.
- *Maven Wrapper*: Use .\mvnw.cmd instead of ./mvnw or mvnw.
- *UTF-8 Encoding*: Configure UTF-8 output (sys.stdout.reconfigure(encoding="utf-8")) in all inline Python scripts executed via PowerShell.