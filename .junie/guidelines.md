# Guidelines

Latte Test Runner - IntelliJ IDE плагін для тестування JavaScript, TypeScript, HTML, CSS коду за допомогою тестового фреймворка Latte (npm package @olton/latte).
Документація щодо використання фреймворку Latte доступна за посиланням https://latte.org.ua

## Можливості плагіна

1. Запуск тестів Latte з контекстного меню файлу, папки або проекту.
2. Запуск тестів Latte з панелі інструментів.
3. Запуск тестів Latte з командного рядка.
4. Запуск тестів Latte з конфігурацій запуску.

## Конфігурація плагіна
1. Можливість вказати шлях до конфігураційного файлу Latte.
2. Вибір версії Node та додаткових параметрів запуску Node.js.
3. Вказівка шляху робочій теки.
4. Вказівка шляху до Latte CLI та додаткових параметрів Latte.
5. Можливість зазначити Environment Variables.
6. Вибір testing scope: all tests, folder, file, test suite, or single test.
    - Можливість вказати додаткові параметри
7. Вибір режиму запуску тестів: run, debug, watch.
8. Вибір DOM environment: jsdom, happy-dom.
9. Вибір coverage mode and coverage reporter: console, junit, lcov, html.

## Підтримка TestExplorer
Плагін Latte Test Runner підтримує TestExplorer, що дозволяє інтегрувати тести Latte в TestExplorer IDE. Це дозволяє зручно переглядати, запускати та керувати тестами Latte безпосередньо з TestExplorer.

### Можливлості
1. Перегляд тестів Latte в TestExplorer.
2. Запуск тестів Latte з TestExplorer.
3. Керування тестами Latte з TestExplorer.
4. Перехід до коду теста з TestExplorer.

