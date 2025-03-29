# Hello World JetBrains Plugin Template

This is a simple Hello World template for a JetBrains plugin that will work with any of their IDEs. It demonstrates the basic structure and components needed for a JetBrains plugin.

## Project Structure

- `build.gradle`: Gradle build configuration for the plugin
- `settings.gradle`: Gradle settings for the project
- `src/main/java/org/example/HelloWorldAction.java`: The action class that displays a "Hello World" message
- `src/main/resources/META-INF/plugin.xml`: Plugin configuration file

## Features

- Adds a "Hello World" action to the Tools menu
- Provides a keyboard shortcut (Ctrl+Alt+H) to trigger the action
- Displays a simple message dialog when the action is triggered

## Building the Plugin

To build the plugin, run:

```bash
./gradlew build
```

This will create a plugin ZIP file in the `build/distributions` directory.

## Installing the Plugin

1. Open any JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.)
2. Go to Settings/Preferences > Plugins
3. Click on the gear icon and select "Install Plugin from Disk..."
4. Navigate to the `build/distributions` directory and select the ZIP file
5. Restart the IDE when prompted

## Using the Plugin

After installing the plugin:

1. Go to the Tools menu and click on "Hello World", or
2. Use the keyboard shortcut Ctrl+Alt+H

A message dialog will appear with the text "Hello, World!".

## Customizing the Plugin

To customize the plugin:

1. Update the plugin metadata in `plugin.xml`
2. Modify the `HelloWorldAction` class to change the behavior
3. Add more actions or extensions as needed

## Requirements

- Java 11 or later
- Gradle 7.0 or later
- Any JetBrains IDE for testing

## License

This template is available under the MIT License.