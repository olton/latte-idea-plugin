package ua.com.pimenov.latte.data

class LatteConfig {
    var verbose: Boolean = false
    var dom: Boolean = false
    var react: Boolean = false
    var coverage: Boolean = false
    var skipPassed: Boolean = false
    var parallel: Boolean = false
    var watch: Boolean = false
    var debug: Boolean = false
    var loader: Boolean = false
    var ts: Boolean = false
    var include: String = ""
    var exclude: String = "node_modules/**"
    var skip: String = ""
    var test: String = ""
    var suite: String = ""
    var reportType: String = "console"
    var reportDir: String = "coverage"
    var reportFile: String = ""
    var maxWorkers: Int = 4
    var progress: String = "none"
    var clearConsole: Boolean = true
    var idea: Boolean = true
    var showStack: Boolean = false

    operator fun set(key: String, value: Any?) {
        when (key) {
            "verbose" -> verbose = (value as? Boolean) ?: verbose
            "dom" -> dom = (value as? Boolean) ?: dom
            "react" -> react = (value as? Boolean) ?: react
            "coverage" -> coverage = (value as? Boolean) ?: coverage
            "skipPassed" -> skipPassed = (value as? Boolean) ?: skipPassed
            "parallel" -> parallel = (value as? Boolean) ?: parallel
            "watch" -> watch = (value as? Boolean) ?: watch
            "debug" -> debug = (value as? Boolean) ?: debug
            "loader" -> loader = (value as? Boolean) ?: loader
            "ts" -> ts = (value as? Boolean) ?: ts
            "include" -> include = (value as? String) ?: include
            "exclude" -> exclude = (value as? String) ?: exclude
            "skip" -> skip = (value as? String) ?: skip
            "test" -> test = (value as? String) ?: test
            "suite" -> suite = (value as? String) ?: suite
            "reportType" -> reportType = (value as? String) ?: reportType
            "reportDir" -> reportDir = (value as? String) ?: reportDir
            "reportFile" -> reportFile = (value as? String) ?: reportFile
            "maxWorkers" -> maxWorkers = (value as? Int) ?: maxWorkers
            "progress" -> progress = (value as? String) ?: progress
            "clear" -> clearConsole = (value as? Boolean) ?: clearConsole
            "idea" -> idea = (value as? Boolean) ?: idea
        }
    }
}