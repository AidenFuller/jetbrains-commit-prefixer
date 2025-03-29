package org.cobalyte;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class for commit message prefixing operations.
 * This class centralizes the logic for getting the current branch name and prefixing commit messages.
 */
public class CommitPrefixerUtil {
    private static final Logger LOG = Logger.getInstance(CommitPrefixerUtil.class);

    /**
     * Gets the current branch name using Git command line.
     *
     * @param project The current project
     * @return The current branch name, or null if it couldn't be determined
     */
    public static String getCurrentBranchName(Project project) {
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
     *
     * @param branchName The current branch name
     * @param commitMessage The original commit message
     * @param settings The commit prefixer settings
     * @return The prefixed commit message, or the original message if prefixing failed
     */
    public static String prefixCommitMessage(String branchName, String commitMessage, CommitPrefixerSettings settings) {
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

    /**
     * Adds a prefix to the commit message if it doesn't already have one.
     *
     * @param project The current project
     * @param commitMessage The original commit message
     * @return The prefixed commit message, or the original message if prefixing failed or wasn't needed
     */
    public static String addPrefixToCommitMessage(Project project, String commitMessage) {
        CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);
        if (!settings.isEnabled()) {
            return commitMessage;
        }

        try {
            // Get the current branch name
            String branchName = getCurrentBranchName(project);
            if (branchName == null || branchName.isEmpty()) {
                return commitMessage;
            }

            // Generate the prefixed message
            String prefix = prefixCommitMessage(branchName, "", settings);
            if (prefix == null || prefix.isEmpty()) {
                return commitMessage;
            }

            // Check if the prefix already exists in the commit message
            if (commitMessage.startsWith(prefix)) {
                return commitMessage;
            }

            // Add the prefix to the commit message
            return prefix + commitMessage;
        } catch (Exception e) {
            LOG.error("Error adding prefix to commit message", e);
            return commitMessage;
        }
    }
}