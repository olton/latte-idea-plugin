package ua.com.pimenov.latte.runs.all

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class AllConfigurationOptions : RunConfigurationOptions() {
    private var _configFile: StoredProperty<String?> = string("").provideDelegate(this, "configFile")
    private var _nodeInterpreter: StoredProperty<String?> = string("").provideDelegate(this, "nodeInterpreter")
    private var _nodeOptions: StoredProperty<String?> = string("").provideDelegate(this, "nodeOptions")
    private var _lattePath: StoredProperty<String?> = string("").provideDelegate(this, "lattePath")
    private var _workingDirectory: StoredProperty<String?> = string("").provideDelegate(this, "workingDirectory")
    private var _latteOptions: StoredProperty<String?> = string("").provideDelegate(this, "latteOptions")
    private var _envVariables: StoredProperty<MutableMap<String, String>> = map<String, String>().provideDelegate(this, "envVariables")
    private var _testScope: StoredProperty<String?> = string("").provideDelegate(this, "testScope")
    private var _scopeDirectory: StoredProperty<String?> = string("").provideDelegate(this, "scopeDirectory")
    private var _scopeFile: StoredProperty<String?> = string("").provideDelegate(this, "scopeFile")
    private var _scopeSuite: StoredProperty<String?> = string("").provideDelegate(this, "scopeSuite")
    private var _scopeTest: StoredProperty<String?> = string("").provideDelegate(this, "scopeTest")

    var configFile: String?
        get() = _configFile.getValue(this) ?: ""
        set(value) = _configFile.setValue(this, value ?: "")

    var nodeInterpreter: String?
        get() = _nodeInterpreter.getValue(this) ?: ""
        set(value) = _nodeInterpreter.setValue(this, value ?: "")

    var nodeOptions: String?
        get() = _nodeOptions.getValue(this) ?: ""
        set(value) = _nodeOptions.setValue(this, value ?: "")

    var lattePath: String?
        get() = _lattePath.getValue(this) ?: ""
        set(value) = _lattePath.setValue(this, value ?: "")

    var workingDirectory: String?
        get() = _workingDirectory.getValue(this) ?: ""
        set(value) = _workingDirectory.setValue(this, value ?: "")

    var latteOptions: String?
        get() = _latteOptions.getValue(this) ?: ""
        set(value) = _latteOptions.setValue(this, value ?: "")

    var envVariables: MutableMap<String, String>
        get() = _envVariables.getValue(this)
        set(value) = _envVariables.setValue(this, value)

    var testScope: String?
        get() = _testScope.getValue(this) ?: ""
        set(value) = _testScope.setValue(this, value ?: "")

    var scopeDirectory: String?
        get() = _scopeDirectory.getValue(this) ?: ""
        set(value) = _scopeDirectory.setValue(this, value ?: "")

    var scopeFile: String?
        get() = _scopeFile.getValue(this) ?: ""
        set(value) = _scopeFile.setValue(this, value ?: "")

    var scopeSuite: String?
        get() = _scopeSuite.getValue(this) ?: ""
        set(value) = _scopeSuite.setValue(this, value ?: "")

    var scopeTest: String?
        get() = _scopeTest.getValue(this) ?: ""
        set(value) = _scopeTest.setValue(this, value ?: "")
}