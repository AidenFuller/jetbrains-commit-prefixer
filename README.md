A plugin for JetBrains IDEs that automatically prefixes commit messages with information extracted from branch names. 

You can find the plugin here: https://plugins.jetbrains.com/plugin/26938-commit-message-prefixer

Features:
- Extract information from branch names using configurable regex patterns
- Format commit messages using configurable format patterns
- Enable/disable the plugin from the commit dialog

The branch name extraction pattern is configurable from the IDE settings

Example:
- Branch name: `feature/12345-add-new-feature`
- Commit message: `Initial implementation`
- Result: `#12345 - Initial implementation`
