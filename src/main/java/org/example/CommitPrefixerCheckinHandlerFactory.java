package org.example;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
            
            // Pre-fill the commit message if the PRE_FILL mode is selected
            CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);
            if (settings.isEnabled() && settings.getPrefixingMode() == CommitPrefixerSettings.PrefixingMode.PRE_FILL) {
                try {
                    // Get the current branch name
                    String branchName = getCurrentBranchName();
                    if (branchName != null && !branchName.isEmpty()) {
                        // Get the current commit message
                        String commitMessage = panel.getCommitMessage();
                        
                        // Apply the prefix to the commit message
                        String prefixedMessage = prefixCommitMessage(branchName, commitMessage, settings);
                        
                        // Set the prefixed commit message
                        panel.setCommitMessage(prefixedMessage);
                    }
                } catch (Exception e) {
                    LOG.error("Error pre-filling commit message", e);
                }
            }
        }

        @Nullable
        @Override
        public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
            CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);

            // Create a checkbox to enable/disable the prefixer for this commit
            JBCheckBox checkBox = new JBCheckBox("Prefix commit message with branch information");
            checkBox.setSelected(settings.isEnabled());
            checkBox.addActionListener(e -> settings.setEnabled(checkBox.isSelected()));

            return new RefreshableOnComponent() {
                @Override
                public JComponent getComponent() {
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(checkBox, BorderLayout.WEST);
                    return panel;
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

            // Skip if the plugin is disabled or if the mode is PRE_FILL
            if (!settings.isEnabled() || settings.getPrefixingMode() == CommitPrefixerSettings.PrefixingMode.PRE_FILL) {
                return ReturnResult.COMMIT;
            }

            try {
                // Get the current branch name
                String branchName = getCurrentBranchName();
                if (branchName == null || branchName.isEmpty()) {
                    LOG.warn("Could not determine current branch name");
                    return ReturnResult.COMMIT;
                }

                // Get the current commit message
                String commitMessage = panel.getCommitMessage();

                // Apply the prefix to the commit message
                String prefixedMessage = prefixCommitMessage(branchName, commitMessage, settings);

                // Set the prefixed commit message
                panel.setCommitMessage(prefixedMessage);

            } catch (Exception e) {
                LOG.error("Error prefixing commit message", e);
            }

            return ReturnResult.COMMIT;
        }

        /**
         * Gets the current branch name using Git command line.
         */
        @Nullable
        private String getCurrentBranchName() {
            try {
                // Get the project base directory
                String basePath = project.getBasePath();
                if (basePath == null) {
                    return null;
                }

                // Run 'git branch --show-current' command
                ProcessBuilder processBuilder = new ProcessBuilder("git", "branch", "--show-current");
                processBuilder.directory(new File(basePath));
                Process process = processBuilder.start();

                // Read the output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String branch = reader.readLine();
                    process.waitFor();
                    return branch;
                }
            } catch (Exception e) {
                LOG.error("Error getting current branch name", e);
                return null;
            }
        }

        /**
         * Prefixes the commit message with information extracted from the branch name.
         */
        private String prefixCommitMessage(String branchName, String commitMessage, CommitPrefixerSettings settings) {
            try {
                // Compile the regex pattern
                Pattern pattern = Pattern.compile(settings.getBranchExtractionPattern());
                Matcher matcher = pattern.matcher(branchName);

                // If the pattern matches, extract the information and format the commit message
                if (matcher.matches()) {
                    String format = settings.getCommitMessageFormat();

                    // Replace $1, $2, etc. with the corresponding capture groups
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        format = format.replace("$" + i, matcher.group(i));
                    }

                    // Replace $MESSAGE with the original commit message
                    format = format.replace("$MESSAGE", commitMessage);

                    return format;
                }
            } catch (PatternSyntaxException e) {
                LOG.error("Invalid regex pattern: " + settings.getBranchExtractionPattern(), e);
            }

            // If the pattern doesn't match or there's an error, return the original commit message
            return commitMessage;
        }
    }
}