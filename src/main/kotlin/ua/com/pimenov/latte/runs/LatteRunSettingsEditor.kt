package ua.com.pimenov.latte.runs

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.awt.FlowLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSeparator
import ua.com.pimenov.latte.Latte
import java.awt.BorderLayout
import java.awt.Cursor
import javax.swing.JButton
import java.awt.Dimension
import com.intellij.ui.components.JBTextField
import ua.com.pimenov.latte.utils.NodeJS

enum class ScopeType(val id: String) {
    ALL("all"),
    DIRECTORY("dir"),
    FILE("file"),
    SUITE("suite"),
    TEST("test");

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromString(value: String?): ScopeType {
            return entries.find { it.id == value } ?: ALL
        }
    }
}

class RunSettingsEditor(private val project: Project) : SettingsEditor<LatteRunConfiguration>() {
    private val topPanel: JPanel

    private val actionPanel: JPanel
    private val configFile: TextFieldWithBrowseButton
    private val configFileEditor = JBTextField()
    private val nodeInterpreter = NodeJsInterpreterField(project, true, true)
    private val nodeOptions = JBTextField()

    private val lattePath: NodePackageField
    private val latteOptions = JBTextField()

    private val workingDirectoryPanel: JPanel
    private val workingDirectory: TextFieldWithBrowseButton
    private val workingDirectoryEditor = JBTextField()
    private val setProjectDirButton = JButton(AllIcons.Actions.ProjectDirectory)

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


    private val scopeDirectory: TextFieldWithBrowseButton
    private val scopeDirectoryEditor = JBTextField()

    private val scopeFile: TextFieldWithBrowseButton
    private val scopeFileEditor = JBTextField()

    private val scopeSuiteFile: TextFieldWithBrowseButton
    private val scopeSuiteFileEditor = JBTextField()
    private val scopeSuiteName = JBTextField()

    private val scopeTestFile: TextFieldWithBrowseButton
    private val scopeTestFileEditor = JBTextField()
    private val scopeTestName = JBTextField()

    private val scopeAllPanel: JPanel
    private val scopeDirectoryPanel: JPanel
    private val scopeFilePanel: JPanel
    private val scopeSuitePanel: JPanel
    private val scopeTestPanel: JPanel

    private val folderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    private val fileDescriptorConf = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
    private val fileDescriptorTest = FileChooserDescriptorFactory.singleFile()
        .withExtensionFilter("Tests file", "js", "ts", "jsx", "tsx")

    init {
        folderDescriptor.title = "Select Directory"
        fileDescriptorConf.title = "Select Config File"
        fileDescriptorTest.title = "Select Test File"

        // Latte Config file
        configFileEditor.emptyText.text = Latte.message("latte.settings.config.file.placeholder")
        configFile = TextFieldWithBrowseButton( configFileEditor )
        configFile.addBrowseFolderListener(
            project,
            fileDescriptorConf
        )

        // Ініціалізація nodeInterpreter значенням за замовчуванням
        val nodeJsInterpreter = NodeJS.getNodeJsInterpreter(project)
        if (nodeJsInterpreter != null) {
            nodeInterpreter.interpreterRef = NodeJS.getNodeJsInterpreter(project)?.toRef()
                ?: NodeJsInterpreterRef.createProjectRef()
        }
        // Node options
        nodeOptions.emptyText.text = Latte.message("latte.settings.node.options.placeholder")

        // Working directory
        workingDirectoryEditor.emptyText.text = Latte.message("latte.settings.working.directory.placeholder")
        workingDirectory = TextFieldWithBrowseButton(workingDirectoryEditor)
        workingDirectoryPanel = JPanel(BorderLayout())
        workingDirectoryPanel.add(workingDirectory, BorderLayout.CENTER)
        workingDirectoryPanel.add(setProjectDirButton, BorderLayout.EAST)
        setProjectDirButton.addActionListener { workingDirectory.text = project.basePath ?: "" }
        setProjectDirButton.toolTipText = Latte.message("latte.settings.working.directory.set.project.dir")
        setProjectDirButton.preferredSize = Dimension(workingDirectory.preferredSize.height, workingDirectory.preferredSize.height)
        setProjectDirButton.cursor = Cursor(Cursor.HAND_CURSOR)
        workingDirectory.text = project.basePath ?: ""
        workingDirectory.addBrowseFolderListener(
            project,
            folderDescriptor
        )

        // Latte options
        lattePath = NodePackageField(project, "@olton/latte", null)
        latteOptions.emptyText.text = Latte.message("latte.settings.latte.options.placeholder")

        // Create the action panel
        actionPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(Latte.message("latte.settings.config.file"), configFile)
            .addComponent(JSeparator())
            .addLabeledComponent(Latte.message("latte.settings.node.interpreter"), nodeInterpreter)
            .addLabeledComponent(Latte.message("latte.settings.node.options"), nodeOptions)
            .addLabeledComponent(Latte.message("latte.settings.working.directory"), workingDirectoryPanel)
            .addLabeledComponent(Latte.message("latte.settings.latte.path"), lattePath)
            .addLabeledComponent(Latte.message("latte.settings.latte.options"), latteOptions)
            .addLabeledComponent(Latte.message("latte.settings.env.variables"), envVariables)
            .addComponent(JSeparator())
            .getPanel()


        // Tests Scope
        scopeSuiteName.emptyText.text = Latte.message("latte.settings.scope.suite.name.placeholder")
        scopeTestName.emptyText.text = Latte.message("latte.settings.scope.test.name.placeholder")

        scopeDirectoryEditor.emptyText.text = Latte.message("latte.settings.scope.directory.placeholder")
        scopeDirectory = TextFieldWithBrowseButton(scopeDirectoryEditor)
        scopeDirectory.addBrowseFolderListener(
            project,
            folderDescriptor
        )

        scopeFileEditor.emptyText.text = Latte.message("latte.settings.scope.file.placeholder")
        scopeFile = TextFieldWithBrowseButton(scopeFileEditor)
        scopeFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

        scopeSuiteFileEditor.emptyText.text = Latte.message("latte.settings.scope.suite.file.placeholder")
        scopeSuiteFile = TextFieldWithBrowseButton(scopeSuiteFileEditor)
        scopeSuiteFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

        scopeTestFileEditor.emptyText.text = Latte.message("latte.settings.scope.test.file.placeholder")
        scopeTestFile = TextFieldWithBrowseButton(scopeTestFileEditor)
        scopeTestFile.addBrowseFolderListener(
            project,
            fileDescriptorTest
        )

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
        scopeTypePanelLayout.vgap = 10

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

    override fun resetEditorFrom(conf: LatteRunConfiguration) {
        configFile.text = conf.configFile ?: ""
        nodeInterpreter.interpreterRef = NodeJsInterpreterRef.create(conf.nodeInterpreter)
        nodeOptions.text = conf.nodeOptions
        lattePath.selected = NodePackage(conf.lattePath ?: (project.basePath + "/node_modules/@olton/latte"))
        workingDirectory.text = conf.workingDirectory ?: project.basePath ?: ""
        latteOptions.text = conf.latteOptions
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

        scopeDirectory.text = conf.testsDirectory ?: ""
        scopeFile.text = conf.testsFile ?: ""
        scopeSuiteFile.text = conf.suiteFile ?: ""
        scopeSuiteName.text = conf.suiteName ?: ""
        scopeTestFile.text = conf.testFile ?: ""
        scopeTestName.text = conf.testName ?: ""
    }

    override fun applyEditorTo(@NotNull conf: LatteRunConfiguration) {
        conf.configFile = configFile.text
        conf.nodeInterpreter = nodeInterpreter.interpreter?.referenceName
        conf.nodeOptions = nodeOptions.text
        conf.lattePath = lattePath.selected.systemIndependentPath
        conf.workingDirectory = workingDirectory.text
        conf.latteOptions = latteOptions.text
        conf.envVariables = envVariables.envs
        conf.testScope = selectedScope.id
        conf.testsDirectory = scopeDirectory.text
        conf.testsFile = scopeFile.text
        conf.suiteFile = scopeSuiteFile.text
        conf.suiteName = scopeSuiteName.text
        conf.testFile = scopeTestFile.text
        conf.testName = scopeTestName.text
    }

    @NotNull
    override fun createEditor(): JComponent {
        return topPanel
    }
}