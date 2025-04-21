package ua.com.pimenov.latte.utils

fun parseArgs(argsString: String): Map<String, String?> {
    val result = mutableMapOf<String, String?>()
    var i = 0

    while (i < argsString.length) {
        // Пропускаємо пробіли
        while (i < argsString.length && argsString[i].isWhitespace()) i++
        if (i >= argsString.length) break

        // Перевіряємо, чи це аргумент (починається з -)
        if (argsString[i] == '-') {
            val start = i

            // Знаходимо кінець імені аргументу
            while (i < argsString.length && !argsString[i].isWhitespace() && argsString[i] != '=') i++

            val argName = argsString.substring(start, i)

            // Перевіряємо, чи є значення
            if (i < argsString.length && argsString[i] == '=') {
                i++ // Пропускаємо знак =

                // Перевіряємо, чи значення в лапках
                if (i < argsString.length && argsString[i] == '"') {
                    i++ // Пропускаємо відкриваючу лапку
                    val valueStart = i

                    // Шукаємо закриваючу лапку
                    while (i < argsString.length && argsString[i] != '"') i++

                    val value = argsString.substring(valueStart, i)
                    i++ // Пропускаємо закриваючу лапку

                    result[argName] = value
                } else {
                    // Значення без лапок
                    val valueStart = i

                    // Шукаємо кінець значення
                    while (i < argsString.length && !argsString[i].isWhitespace()) i++

                    val value = argsString.substring(valueStart, i)
                    result[argName] = value
                }
            } else {
                // Аргумент без значення (прапорець)
                result[argName] = null
            }
        } else {
            // Це не аргумент, пропускаємо
            while (i < argsString.length && !argsString[i].isWhitespace()) i++
        }
    }

    return result
}