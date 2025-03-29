package org.cobalyte;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.NotNull;

/**
 * An action that adds a prefix to the commit message based on the current branch name.
 * This action is added to the commit dialog toolbar for easy access.
 */
public class AddCommitPrefixAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // Get the settings
        org.cobalyte.CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);

        // Only show the button if the plugin is enabled and prefixing mode is MANUAL
        boolean visible = settings.isEnabled() && settings.getPrefixingMode() == CommitPrefixerSettings.PrefixingMode.MANUAL;
        e.getPresentation().setEnabledAndVisible(visible);

        // Set the icon if one is specified
        if (visible) {
            e.getPresentation().setIcon(AllIcons.Duplicates.SendToTheRight);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Get the commit message panel
        Refreshable refreshable = Refreshable.PANEL_KEY.getData(e.getDataContext());
        if (!(refreshable instanceof CheckinProjectPanel panel)) {
            return;
        }

        // Get the settings
        CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);
        if (!settings.isEnabled()) {
            return;
        }

        try {
            // Get the current commit message
            String commitMessage = panel.getCommitMessage();

            // Add the prefix to the commit message
            String prefixedMessage = CommitPrefixerUtil.addPrefixToCommitMessage(project, commitMessage);

            // If the message was changed, update it
            if (!prefixedMessage.equals(commitMessage)) {
                panel.setCommitMessage(prefixedMessage);
            }
        } catch (Exception ex) {
            // Log the error but don't show it to the user
            // We don't want to interrupt the commit process
        }
    }
}
