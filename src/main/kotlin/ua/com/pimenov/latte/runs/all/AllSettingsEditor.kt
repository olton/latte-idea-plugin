package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.awt.FlowLayout
import java.awt.GridBagLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator

class AllSettingsEditor(private val project: Project) : SettingsEditor<AllRunConfiguration?>() {
    private val topPanel: JPanel
    private val configFile: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val actionPanel: JPanel
    private val nodeInterpreter: NodeJsInterpreterField = NodeJsInterpreterField(project, true, true)
    private val nodeOptions: RawCommandLineEditor = RawCommandLineEditor()
    private val lattePath = NodePackageField(project, "latte", null)
    private val workingDirectory: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val latteOptions: RawCommandLineEditor = RawCommandLineEditor()
    private val envVariables: EnvironmentVariablesTextFieldWithBrowseButton = EnvironmentVariablesTextFieldWithBrowseButton()
    private val scopePanel: JPanel
    private var selectedScope: String = "all"
    private val radioAllTests = JRadioButton("All tests")
    private val radioDirectory = JRadioButton("Directory")
    private val radioFile = JRadioButton("Test File")
    private val radioSuite = JRadioButton("Suite")
    private val radioTest = JRadioButton("Test")

    init {
        configFile.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFileDescriptor("json")
        )

        workingDirectory.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        radioAllTests.toolTipText = "Run all tests"
        radioAllTests.setMnemonic('l')
        radioAllTests.setSelected(true)
        radioAllTests.actionCommand = "all"

        radioDirectory.toolTipText = "Run all tests in directory"
        radioDirectory.setMnemonic('D')
        radioDirectory.actionCommand = "dir"

        radioFile.toolTipText = "Run single test file"
        radioFile.setMnemonic('F')
        radioFile.actionCommand = "file"

        radioSuite.toolTipText = "Run all tests in suite"
        radioSuite.setMnemonic('e')
        radioSuite.actionCommand = "suite"

        radioTest.toolTipText = "Run single test"
        radioTest.setMnemonic('T')
        radioTest.actionCommand = "test"

        radioAllTests.addActionListener { selectedScope = "all" }
        radioDirectory.addActionListener { selectedScope = "dir" }
        radioFile.addActionListener { selectedScope = "file" }
        radioSuite.addActionListener { selectedScope = "suite" }
        radioTest.addActionListener { selectedScope = "test" }

        val group = ButtonGroup()

        group.add(radioAllTests)
        group.add(radioDirectory)
        group.add(radioFile)
        group.add(radioSuite)
        group.add(radioTest)

        scopePanel = FormBuilder.createFormBuilder()
            .addComponent(radioAllTests)
            .addComponent(radioDirectory)
            .addComponent(radioFile)
            .addComponent(radioSuite)
            .addComponent(radioTest)
            .getPanel()

        val scopePanelLayout = FlowLayout()
        scopePanelLayout.alignment = FlowLayout.CENTER
        scopePanelLayout.hgap = 40
        scopePanelLayout.vgap = 0

        scopePanel.layout = scopePanelLayout

        actionPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Node interpreter:", nodeInterpreter)
            .addLabeledComponent("Node options:", nodeOptions)
            .addLabeledComponent("Latte path:", lattePath)
            .addLabeledComponent("Working directory:", workingDirectory)
            .addLabeledComponent("Latte options:", latteOptions)
            .addLabeledComponent("Environment variables:", envVariables)
            .addComponent(JSeparator())
            .addComponent(scopePanel)
            .getPanel()

        topPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Config file:", configFile)
            .addComponent(JSeparator())
            .addComponent(actionPanel)
            .getPanel()
    }

    override fun resetEditorFrom(conf: AllRunConfiguration) {
        configFile.text = conf.configFile ?: ""
        nodeInterpreter.interpreterRef = NodeJsInterpreterRef.create(conf.nodeInterpreter ?: "")
        nodeOptions.text = conf.nodeOptions ?: ""
        lattePath.selected = NodePackage(conf.lattePath ?: "")
        workingDirectory.text = conf.workingDirectory ?: ""
        latteOptions.text = conf.latteOptions ?: ""
        envVariables.envs = conf.envVariables
        selectedScope = conf.testScope ?: "all"
        when (selectedScope) {
            "all" -> radioAllTests.isSelected = true
            "dir" -> radioDirectory.isSelected = true
            "file" -> radioFile.isSelected = true
            "suite" -> radioSuite.isSelected = true
            "test" -> radioTest.isSelected = true
        }
    }

    override fun applyEditorTo(@NotNull conf: AllRunConfiguration) {
        conf.configFile = configFile.text
        conf.nodeInterpreter = nodeInterpreter.interpreterRef.referenceName
        conf.nodeOptions = nodeOptions.text
        conf.lattePath = lattePath.selected?.systemIndependentPath
        conf.workingDirectory = workingDirectory.text
        conf.latteOptions = latteOptions.text
        conf.envVariables = envVariables.envs
        conf.testScope = selectedScope
    }

    @NotNull
    override fun createEditor(): JComponent {
        return topPanel
    }
}