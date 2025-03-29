package org.cobalyte;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Factory for creating commit handlers that prefix commit messages with information from branch names.
 */
public class CommitPrefixerCheckinHandlerFactory extends CheckinHandlerFactory {
    private static final Logger LOG = Logger.getInstance(CommitPrefixerCheckinHandlerFactory.class);

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        return new CommitPrefixerCheckinHandler(panel);
    }

    private static class CommitPrefixerCheckinHandler extends CheckinHandler {
        private final CheckinProjectPanel panel;
        private final Project project;

        public CommitPrefixerCheckinHandler(@NotNull CheckinProjectPanel panel) {
            this.panel = panel;
            this.project = panel.getProject();
        }

        @Nullable
        @Override
        public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
            CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);

            // Create a checkbox to enable/disable the prefixer for this commit
            JBCheckBox checkBox = new JBCheckBox("Enable commit message prefixing");
            checkBox.setSelected(settings.isEnabled());
            checkBox.addActionListener(e -> settings.setEnabled(checkBox.isSelected()));

            return new RefreshableOnComponent() {
                @Override
                public JComponent getComponent() {
                    return checkBox;
                }

                @Override
                public void refresh() {
                    checkBox.setSelected(settings.isEnabled());
                }

                @Override
                public void saveState() {
                    settings.setEnabled(checkBox.isSelected());
                }

                @Override
                public void restoreState() {
                    checkBox.setSelected(settings.isEnabled());
                }
            };
        }

        @Override
        public ReturnResult beforeCheckin() {
            CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);

            // Only automatically add the prefix if automatic prefixing is enabled
            if (settings.isEnabled() && settings.getPrefixingMode() == CommitPrefixerSettings.PrefixingMode.AUTOMATIC) {
                try {
                    // Get the current commit message
                    String commitMessage = panel.getCommitMessage();

                    // Add the prefix to the commit message
                    String prefixedMessage = CommitPrefixerUtil.addPrefixToCommitMessage(project, commitMessage);

                    // If the message was changed, update it
                    if (!prefixedMessage.equals(commitMessage)) {
                        panel.setCommitMessage(prefixedMessage);
                    }
                } catch (Exception e) {
                    LOG.error("Error adding prefix to commit message", e);
                }
            }

            return ReturnResult.COMMIT;
        }
    }
}
