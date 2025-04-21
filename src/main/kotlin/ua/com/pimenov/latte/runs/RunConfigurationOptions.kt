package ua.com.pimenov.latte.runs

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class RunConfigurationOptions : RunConfigurationOptions() {
    private var myConfigFile: StoredProperty<String?> = string().provideDelegate(this, "configFile")
    private var myNodeInterpreter: StoredProperty<String?> = string().provideDelegate(this, "nodeInterpreter")
    private var myNodeOptions: StoredProperty<String?> = string().provideDelegate(this, "nodeOptions")
    private var myLattePath: StoredProperty<String?> = string().provideDelegate(this, "lattePath")
    private var myWorkingDirectory: StoredProperty<String?> = string().provideDelegate(this, "workingDirectory")
    private var myLatteOptions: StoredProperty<String?> = string().provideDelegate(this, "latteOptions")
    private var myEnvVariables: StoredProperty<MutableMap<String, String>> = map<String, String>().provideDelegate(this, "envVariables")
    private var myTestScope: StoredProperty<String?> = string().provideDelegate(this, "testScope")
    private var myTestsDirectory: StoredProperty<String?> = string("").provideDelegate(this, "testsDirectory")
    private var myTestsFile: StoredProperty<String?> = string().provideDelegate(this, "testsFile")
    private var mySuiteFile: StoredProperty<String?> = string().provideDelegate(this, "suiteFile")
    private var mySuiteName: StoredProperty<String?> = string().provideDelegate(this, "suiteName")
    private var myTestFile: StoredProperty<String?> = string().provideDelegate(this, "testFile")
    private var myTestName: StoredProperty<String?> = string().provideDelegate(this, "testName")

    var configFile: String?
        get() = myConfigFile.getValue(this)
        set(value) = myConfigFile.setValue(this, value)

    var nodeInterpreter: String
        get() = myNodeInterpreter.getValue(this) ?: ""
        set(value) = myNodeInterpreter.setValue(this, value)

    var nodeOptions: String?
        get() = myNodeOptions.getValue(this)
        set(value) = myNodeOptions.setValue(this, value)

    var lattePath: String?
        get() = myLattePath.getValue(this)
        set(value) = myLattePath.setValue(this, value)

    var workingDirectory: String?
        get() = myWorkingDirectory.getValue(this)
        set(value) = myWorkingDirectory.setValue(this, value)

    var latteOptions: String?
        get() = myLatteOptions.getValue(this)
        set(value) = myLatteOptions.setValue(this, value)

    var envVariables: MutableMap<String, String>
        get() = myEnvVariables.getValue(this)
        set(value) = myEnvVariables.setValue(this, value)

    var testScope: String?
        get() = myTestScope.getValue(this) ?: ScopeType.ALL.id
        set(value) = myTestScope.setValue(this, value ?: ScopeType.ALL.id)

    var testsDirectory: String?
        get() = myTestsDirectory.getValue(this)
        set(value) = myTestsDirectory.setValue(this, value)

    var testsFile: String?
        get() = myTestsFile.getValue(this)
        set(value) = myTestsFile.setValue(this, value)

    var suiteFile: String?
        get() = mySuiteFile.getValue(this)
        set(value) = mySuiteFile.setValue(this, value)

    var suiteName: String?
        get() = mySuiteName.getValue(this)
        set(value) = mySuiteName.setValue(this, value)

    var testFile: String?
        get() = myTestFile.getValue(this)
        set(value) = myTestFile.setValue(this, value)

    var testName: String?
        get() = myTestName.getValue(this)
        set(value) = myTestName.setValue(this, value)
}