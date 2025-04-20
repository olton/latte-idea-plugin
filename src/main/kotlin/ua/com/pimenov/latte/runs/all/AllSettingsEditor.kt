package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
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
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator
import javax.swing.JTextField
import ua.com.pimenov.latte.Latte
import ua.com.pimenov.latte.utils.setPlaceholder

enum class ScopeType(val id: String) {
    ALL("all"),
    DIRECTORY("dir"),
    FILE("file"),
    SUITE("suite"),
    TEST("test");

    companion object {
        fun fromString(value: String?): ScopeType {
            return entries.find { it.id == value } ?: ALL
        }
    }
}

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

    private var selectedScope = ScopeType.ALL

    private val radioAllTests = JRadioButton(Latte.message("latte.settings.scope.all"))
    private val radioDirectory = JRadioButton(Latte.message("latte.settings.scope.directory"))
    private val radioFile = JRadioButton(Latte.message("latte.settings.scope.file"))
    private val radioSuite = JRadioButton(Latte.message("latte.settings.scope.suite"))
    private val radioTest = JRadioButton(Latte.message("latte.settings.scope.test"))

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

        setPlaceholder(workingDirectory.textField, Latte.message("latte.settings.working.directory.placeholder"))
        setPlaceholder(scopeDirectory.textField, Latte.message("latte.settings.scope.directory.placeholder"))
        setPlaceholder(latteOptions.textField, Latte.message("latte.settings.latte.options.placeholder"))
        setPlaceholder(nodeOptions.textField, Latte.message("latte.settings.node.options.placeholder"))
        setPlaceholder(scopeSuiteName, Latte.message("latte.settings.scope.suite.name.placeholder"))
        setPlaceholder(scopeSuiteFile.textField, Latte.message("latte.settings.scope.suite.file.placeholder"))
        setPlaceholder(scopeTestName, Latte.message("latte.settings.scope.test.name.placeholder"))
        setPlaceholder(scopeTestFile.textField, Latte.message("latte.settings.scope.test.file.placeholder"))
        setPlaceholder(scopeFile.textField, Latte.message("latte.settings.scope.test.file.placeholder"))

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
            .addLabeledComponent(Latte.message("latte.settings.node.interpreter"), nodeInterpreter)
            .addLabeledComponent(Latte.message("latte.settings.node.options"), nodeOptions)
            .addLabeledComponent(Latte.message("latte.settings.latte.path"), lattePath)
            .addLabeledComponent(Latte.message("latte.settings.working.directory"), workingDirectory)
            .addLabeledComponent(Latte.message("latte.settings.latte.options"), latteOptions)
            .addLabeledComponent(Latte.message("latte.settings.env.variables"), envVariables)
            .addComponent(JSeparator())
            .getPanel()

        radioAllTests.toolTipText = Latte.message("latte.settings.scope.all.tooltip")
        radioAllTests.setMnemonic('l')
        radioAllTests.setSelected(true)
        radioAllTests.actionCommand = ScopeType.ALL.id

        radioDirectory.toolTipText = Latte.message("latte.settings.scope.directory.tooltip")
        radioDirectory.setMnemonic('D')
        radioDirectory.actionCommand = ScopeType.DIRECTORY.id

        radioFile.toolTipText = Latte.message("latte.settings.scope.file.tooltip")
        radioFile.setMnemonic('F')
        radioFile.actionCommand = ScopeType.FILE.id

        radioSuite.toolTipText = Latte.message("latte.settings.scope.suite.tooltip")
        radioSuite.setMnemonic('e')
        radioSuite.actionCommand = ScopeType.SUITE.id

        radioTest.toolTipText = Latte.message("latte.settings.scope.test.tooltip")
        radioTest.setMnemonic('T')
        radioTest.actionCommand = ScopeType.TEST.id

        radioAllTests.addActionListener { updateScopeVisibility(ScopeType.ALL) }
        radioDirectory.addActionListener { updateScopeVisibility(ScopeType.DIRECTORY) }
        radioFile.addActionListener { updateScopeVisibility(ScopeType.FILE) }
        radioSuite.addActionListener { updateScopeVisibility(ScopeType.SUITE) }
        radioTest.addActionListener { updateScopeVisibility(ScopeType.TEST) }

        val group = ButtonGroup()

        group.add(radioAllTests)
        group.add(radioDirectory)
        group.add(radioFile)
        group.add(radioSuite)
        group.add(radioTest)

        scopeAllPanel = FormBuilder.createFormBuilder().getPanel()

        scopeDirectoryPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(Latte.message("latte.settings.scope.directory.label"), scopeDirectory)
            .getPanel()

        scopeFilePanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(Latte.message("latte.settings.scope.file.label"), scopeFile)
            .getPanel()

        scopeSuitePanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(Latte.message("latte.settings.scope.file.label"), scopeSuiteFile)
            .addLabeledComponent(Latte.message("latte.settings.scope.suite.name"), scopeSuiteName)
            .getPanel()

        scopeTestPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(Latte.message("latte.settings.scope.file.label"), scopeTestFile)
            .addLabeledComponent(Latte.message("latte.settings.scope.test.name"), scopeTestName)
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
            .addLabeledComponent(Latte.message("latte.settings.config.file"), configFile)
            .addComponent(JSeparator())
            .addComponent(actionPanel)
            .addComponent(scopePanel)
            .getPanel()

    }

    private fun updateScopeVisibility(scope: ScopeType) {
        selectedScope = scope
        scopeAllPanel.isVisible = scope == ScopeType.ALL
        scopeDirectoryPanel.isVisible = scope == ScopeType.DIRECTORY
        scopeFilePanel.isVisible = scope == ScopeType.FILE
        scopeSuitePanel.isVisible = scope == ScopeType.SUITE
        scopeTestPanel.isVisible = scope == ScopeType.TEST
    }

    override fun resetEditorFrom(conf: AllRunConfiguration) {
        configFile.text = conf.configFile ?: ""
        nodeInterpreter.interpreterRef = NodeJsInterpreterRef.create(conf.nodeInterpreter ?: "")
        nodeOptions.text = conf.nodeOptions ?: ""
        lattePath.selected = NodePackage(conf.lattePath ?: "")
        workingDirectory.text = conf.workingDirectory ?: ""
        latteOptions.text = conf.latteOptions ?: ""
        envVariables.envs = conf.envVariables
        selectedScope = ScopeType.fromString(conf.testScope)
        when (selectedScope) {
            ScopeType.ALL -> radioAllTests.isSelected = true
            ScopeType.DIRECTORY -> radioDirectory.isSelected = true
            ScopeType.FILE -> radioFile.isSelected = true
            ScopeType.SUITE -> radioSuite.isSelected = true
            ScopeType.TEST -> radioTest.isSelected = true
        }
        updateScopeVisibility(selectedScope)
    }

    override fun applyEditorTo(@NotNull conf: AllRunConfiguration) {
        conf.configFile = configFile.text
        conf.nodeInterpreter = nodeInterpreter.interpreterRef.referenceName
        conf.nodeOptions = nodeOptions.text
        conf.lattePath = lattePath.selected.systemIndependentPath
        conf.workingDirectory = workingDirectory.text
        conf.latteOptions = latteOptions.text
        conf.envVariables = envVariables.envs
        conf.testScope = selectedScope.id
    }

    @NotNull
    override fun createEditor(): JComponent {
        return topPanel
    }
}