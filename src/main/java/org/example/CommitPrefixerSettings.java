package org.example;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent settings for the Commit Prefixer plugin.
 * Stores the regex pattern for extracting information from branch names
 * and the format pattern for the commit message prefix.
 */
@State(
    name = "CommitPrefixerSettings",
    storages = {@Storage("commitPrefixerSettings.xml")}
)
public class CommitPrefixerSettings implements PersistentStateComponent<CommitPrefixerSettings> {

    // Default regex pattern: matches "bug/12345-description" or "feature/12345-description"
    private String branchExtractionPattern = "(bug|feature)/(\\d+)-(.+)";

    // Default format pattern: uses the second capture group from the regex
    private String commitMessageFormat = "#$2 - $MESSAGE";

    // Whether the plugin is enabled
    private boolean enabled = true;

    // The prefixing mode
    private PrefixingMode prefixingMode = PrefixingMode.MANUAL;

    // Enum for prefixing modes
    public enum PrefixingMode {
        MANUAL("Pre-fill in commit dialog"),
        AUTOMATIC("Before commit");

        private final String displayName;

        PrefixingMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    public static CommitPrefixerSettings getInstance(Project project) {
        return project.getService(CommitPrefixerSettings.class);
    }

    @Nullable
    @Override
    public CommitPrefixerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CommitPrefixerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getBranchExtractionPattern() {
        return branchExtractionPattern;
    }

    public void setBranchExtractionPattern(String branchExtractionPattern) {
        this.branchExtractionPattern = branchExtractionPattern;
    }

    public String getCommitMessageFormat() {
        return commitMessageFormat;
    }

    public void setCommitMessageFormat(String commitMessageFormat) {
        this.commitMessageFormat = commitMessageFormat;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PrefixingMode getPrefixingMode() {
        return prefixingMode;
    }

    public void setPrefixingMode(PrefixingMode prefixingMode) {
        this.prefixingMode = prefixingMode;
    }
}
