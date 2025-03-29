package org.cobalyte;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a settings UI for the Commit Prefixer plugin.
 */
public class CommitPrefixerConfigurable implements Configurable {

    private final Project project;
    private JBTextField branchExtractionPatternField;
    private JBTextField commitMessageFormatField;
    private JBCheckBox enabledCheckBox;
    private ComboBox<CommitPrefixerSettings.PrefixingMode> prefixingModeComboBox;

    public CommitPrefixerConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Commit Prefixer";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        branchExtractionPatternField = new JBTextField();
        commitMessageFormatField = new JBTextField();
        enabledCheckBox = new JBCheckBox("Enable commit message prefixing");

        // Create a dropdown for prefixing mode
        prefixingModeComboBox = new ComboBox<>(CommitPrefixerSettings.PrefixingMode.values());

        // Set a custom renderer to display the enum's display name instead of its toString() value
        prefixingModeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CommitPrefixerSettings.PrefixingMode) {
                    setText(((CommitPrefixerSettings.PrefixingMode) value).getDisplayName());
                }
                return this;
            }
        });

        // Add help text
        JBLabel branchPatternHelp = new JBLabel("<html>Pattern to extract information from branch names.<br>" +
                "Example: (bug|feature)/(\\d+)-(.+)<br>" +
                "This matches 'bug/12345-description' or 'feature/12345-description'</html>");
        branchPatternHelp.setForeground(JBColor.GRAY);

        JBLabel formatHelp = new JBLabel("<html>Format for the commit message prefix.<br>" +
                "Use $1, $2, etc. to reference capture groups from the regex pattern.<br>" +
                "Use $MESSAGE to reference the original commit message.<br>" +
                "Example: #$2 - $MESSAGE</html>");
        formatHelp.setForeground(JBColor.GRAY);

        JBLabel prefixingHelp = new JBLabel("<html>Select how the prefix should be added to commit messages:<br>" +
                "<b>Pre-fill in commit dialog</b>: You need to click the 'Add Prefix' button in the commit dialog.<br>" +
                "<b>Before commit</b>: The prefix will be added automatically before commit.</html>");
        prefixingHelp.setForeground(JBColor.GRAY);

        // Build the form
        FormBuilder builder = FormBuilder.createFormBuilder()
                .addComponent(enabledCheckBox)
                .addLabeledComponent("Prefixing Mode:", prefixingModeComboBox)
                .addComponent(prefixingHelp)
                .addLabeledComponent("Branch Extraction Pattern:", branchExtractionPatternField)
                .addComponent(branchPatternHelp)
                .addLabeledComponent("Commit Message Format:", commitMessageFormatField)
                .addComponent(formatHelp);

        return builder.getPanel();
    }

    @Override
    public boolean isModified() {
        CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);

        return !branchExtractionPatternField.getText().equals(settings.getBranchExtractionPattern()) ||
               !commitMessageFormatField.getText().equals(settings.getCommitMessageFormat()) ||
               enabledCheckBox.isSelected() != settings.isEnabled() ||
               prefixingModeComboBox.getSelectedItem() != settings.getPrefixingMode();
    }

    @Override
    public void apply() throws ConfigurationException {
        CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);
        settings.setBranchExtractionPattern(branchExtractionPatternField.getText());
        settings.setCommitMessageFormat(commitMessageFormatField.getText());
        settings.setEnabled(enabledCheckBox.isSelected());
        settings.setPrefixingMode((CommitPrefixerSettings.PrefixingMode) prefixingModeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        CommitPrefixerSettings settings = CommitPrefixerSettings.getInstance(project);
        branchExtractionPatternField.setText(settings.getBranchExtractionPattern());
        commitMessageFormatField.setText(settings.getCommitMessageFormat());
        enabledCheckBox.setSelected(settings.isEnabled());
        prefixingModeComboBox.setSelectedItem(settings.getPrefixingMode());
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Commit Prefixer Settings";
    }
}
