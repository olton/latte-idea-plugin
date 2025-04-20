package ua.com.pimenov.latte.data

class Config {
    var verbose: Boolean = false
    var dom: Boolean = false
    var react: Boolean = false
    var coverage: Boolean = false
    var skipPassed: Boolean = false
    var parallel: Boolean = false
    var watch: Boolean = false
    var debug: Boolean = false
    var include: String = ""
    var exclude: String = ""
    var skip: String = ""
    var test: String = ""
    var reportType: String = "console"
    var reportDir: String = "coverage"
    var reportFile: String = ""
    var maxWorkers: Int = 4
    var progress: String = "default"

    companion object {
        const val CONFIG_FILE_NAME = "latte.json"
    }
}