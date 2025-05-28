package ua.com.pimenov.latte.utils

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.project.Project

class NodeJS {
    companion object {
        /**
         * Returns the Node.js interpreter for the given project.
         * If no interpreter is found, returns null.
         */
        fun getNodeJsInterpreter(project: Project): NodeJsLocalInterpreter? {
            return NodeJsInterpreterManager.getInstance(project).interpreter as? NodeJsLocalInterpreter
        }

        fun getNodePath(project: Project): String {
            val interpreter = getNodeJsInterpreter(project)
            return interpreter?.interpreterSystemDependentPath ?: ""
        }

        fun getNode(project: Project): String {
            val interpreter = getNodeJsInterpreter(project)
            return interpreter?.referenceName ?: ""
        }
    }
}