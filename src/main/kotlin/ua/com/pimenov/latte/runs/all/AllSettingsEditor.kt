package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.awt.FlowLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator
import javax.swing.JTextField

class AllSettingsEditor(private val project: Project) : SettingsEditor<AllRunConfiguration?>() {
    private val topPanel: JPanel

    private val configFile = TextFieldWithBrowseButton()
    private val actionPanel: JPanel
    private val nodeInterpreter = NodeJsInterpreterField(project, true, true)
    private val nodeOptions = RawCommandLineEditor()
    private val lattePath = NodePackageField(project, "latte", null)
    private val workingDirectory = TextFieldWithBrowseButton()
    private val latteOptions = RawCommandLineEditor()
    private val envVariables = EnvironmentVariablesTextFieldWithBrowseButton()

    private val scopePanel: JPanel
    private val scopeTypePanel: JPanel
    private val scopeDetailsPanel: JPanel

    private var selectedScope: String = "all"
    private val radioAllTests = JRadioButton("All tests")
    private val radioDirectory = JRadioButton("Directory")
    private val radioFile = JRadioButton("Test File")
    private val radioSuite = JRadioButton("Suite")
    private val radioTest = JRadioButton("Test")

    private val scopeDirectory = TextFieldWithBrowseButton()
    private val scopeFile = TextFieldWithBrowseButton()
    private val scopeSuiteFile = TextFieldWithBrowseButton()
    private val scopeSuiteName = JTextField()
    private val scopeTestFile = TextFieldWithBrowseButton()
    private val scopeTestName = JTextField()

    private val scopeAllPanel: JPanel
    private val scopeDirectoryPanel: JPanel
    private val scopeFilePanel: JPanel
    private val scopeSuitePanel: JPanel
    private val scopeTestPanel: JPanel

    init {
        val folderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        val fileDescriptorConf = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
        val fileDescriptorTest = FileChooserDescriptorFactory.singleFile().withExtensionFilter("Select test file...", "js", "ts", "jsx", "tsx")

        configFile.addBrowseFolderListener(
            project,
            fileDescriptorConf
        )

        workingDirectory.addBrowseFolderListener(
            project,
            folderDescriptor
        )

        scopeDirectory.addBrowseFolderListener(
            project,
            folderDescriptor
        )

        scopeFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

        scopeSuiteFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

        scopeTestFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

        actionPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Node interpreter:", nodeInterpreter)
            .addLabeledComponent("Node options:", nodeOptions)
            .addLabeledComponent("Latte path:", lattePath)
            .addLabeledComponent("Working directory:", workingDirectory)
            .addLabeledComponent("Latte options:", latteOptions)
            .addLabeledComponent("Environment variables:", envVariables)
            .addComponent(JSeparator())
            .getPanel()

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

        radioAllTests.addActionListener {
            selectedScope = "all"
            scopeAllPanel.setVisible(true)
            scopeDirectoryPanel.setVisible(false)
            scopeFilePanel.setVisible(false)
            scopeSuitePanel.setVisible(false)
            scopeTestPanel.setVisible(false)
        }
        radioDirectory.addActionListener {
            selectedScope = "dir"
            scopeAllPanel.setVisible(false)
            scopeDirectoryPanel.setVisible(true)
            scopeFilePanel.setVisible(false)
            scopeSuitePanel.setVisible(false)
            scopeTestPanel.setVisible(false)
        }
        radioFile.addActionListener {
            selectedScope = "file"
            scopeAllPanel.setVisible(false)
            scopeDirectoryPanel.setVisible(false)
            scopeFilePanel.setVisible(true)
            scopeSuitePanel.setVisible(false)
            scopeTestPanel.setVisible(false)
        }
        radioSuite.addActionListener {
            selectedScope = "suite"
            scopeAllPanel.setVisible(false)
            scopeDirectoryPanel.setVisible(false)
            scopeFilePanel.setVisible(false)
            scopeSuitePanel.setVisible(true)
            scopeTestPanel.setVisible(false)
        }
        radioTest.addActionListener {
            selectedScope = "test"
            scopeAllPanel.setVisible(false)
            scopeDirectoryPanel.setVisible(false)
            scopeFilePanel.setVisible(false)
            scopeSuitePanel.setVisible(false)
            scopeTestPanel.setVisible(true)
        }

        val group = ButtonGroup()

        group.add(radioAllTests)
        group.add(radioDirectory)
        group.add(radioFile)
        group.add(radioSuite)
        group.add(radioTest)

        scopeAllPanel = FormBuilder.createFormBuilder().getPanel()

        scopeDirectoryPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Directory:", scopeDirectory)
            .getPanel()

        scopeFilePanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Test file:", scopeFile)
            .getPanel()

        scopeSuitePanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Test file:", scopeSuiteFile)
            .addLabeledComponent("Suite(s):", scopeSuiteName)
            .getPanel()

        scopeTestPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Test file:", scopeTestFile)
            .addLabeledComponent("Test name:", scopeTestName)
            .getPanel()

        scopeTypePanel = FormBuilder.createFormBuilder()
            .addComponent(radioAllTests)
            .addComponent(radioDirectory)
            .addComponent(radioFile)
            .addComponent(radioSuite)
            .addComponent(radioTest)
            .addSeparator()
            .getPanel()

        val scopeTypePanelLayout = FlowLayout()
        scopeTypePanelLayout.alignment = FlowLayout.CENTER
        scopeTypePanelLayout.hgap = 40
        scopeTypePanelLayout.vgap = 0

        scopeTypePanel.layout = scopeTypePanelLayout

        scopeAllPanel.setVisible(true)
        scopeDirectoryPanel.setVisible(false)
        scopeFilePanel.setVisible(false)
        scopeSuitePanel.setVisible(false)
        scopeTestPanel.setVisible(false)

        scopeDetailsPanel = FormBuilder.createFormBuilder()
            .addComponent(scopeAllPanel)
            .addComponent(scopeDirectoryPanel)
            .addComponent(scopeFilePanel)
            .addComponent(scopeSuitePanel)
            .addComponent(scopeTestPanel)
            .getPanel()

        scopePanel = FormBuilder.createFormBuilder()
            .addComponent(scopeTypePanel)
            .addComponent(scopeDetailsPanel)
            .getPanel()

        topPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Config file:", configFile)
            .addComponent(JSeparator())
            .addComponent(actionPanel)
            .addComponent(scopePanel)
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
        conf.lattePath = lattePath.selected.systemIndependentPath
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