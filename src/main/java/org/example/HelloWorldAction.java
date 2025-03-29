package org.example;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * A simple action that displays a "Hello World" message when triggered.
 */
public class HelloWorldAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Messages.showMessageDialog(
                project,
                "Hello, World!",
                "Hello World Plugin",
                Messages.getInformationIcon()
        );
    }

    @Override
    public String toString() {
        return "";
    }
}