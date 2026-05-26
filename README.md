# Лабораторная работа №5

## Тема

Разработка интеллектуальной игры на основе мультиагентного подхода.

## Выбранный вариант

**Вариант 1: Крестики-нолики.**

Игра выбрана как удобный вариант для проверки мультиагентного взаимодействия: два агента играют друг против друга, поле имеет небольшой размер, а корректность поведения легко проверить по консольному выводу и ACL-сообщениям.

## Что реализовано

Проект содержит Java-приложение с использованием JADE:

- `TicTacToeAgent` — агент-игрок;
- `FindOpponentBehaviour` — поведение поиска второго агента через DF;
- `ReceiveGameMessagesBehaviour` — циклическое поведение приема ACL-сообщений;
- `GameBoard` — модель поля 3x3;
- `MoveSelector` — интеллектуальный выбор хода через алгоритм minimax;
- запуск через стандартный GUI JADE `jade.Boot`.

Агенты обмениваются ACL-сообщениями с `conversation-id = tic-tac-toe`. Один агент запускается с аргументом `starter` и играет за `X`, второй автоматически принимает роль `O`.

## Подготовка

Поместите библиотеку JADE в каталог:

```text
libs/jade.jar
```

Итоговая структура должна быть такой:

```text
Lab5_TicTacToe_JADE/
 ├─ libs/
 │   └─ jade.jar
 ├─ src/
 ├─ build.gradle
 ├─ pom.xml
 └─ .run/
     └─ JADE_GUI.run.xml
```

## Основной запуск через GUI JADE в IntelliJ IDEA

1. Откройте папку проекта `Lab5_TicTacToe_JADE` в IntelliJ IDEA.
2. Проверьте, что `libs/jade.jar` добавлен в проект.
3. Откройте:

```text
Run → Edit Configurations → + → Application
```

4. Укажите:

```text
Name: JADE GUI
Main class: jade.Boot
Program arguments: -gui -agents "playerX:ru.vstu.lab5.agents.TicTacToeAgent(starter);playerO:ru.vstu.lab5.agents.TicTacToeAgent"
```

5. Запустите конфигурацию `JADE GUI`.

После запуска должно открыться окно **JADE Remote Management Agent (RMA)**. В списке агентов должны быть:

```text
ams
df
rma
playerX
playerO
```

В консоли будет виден процесс игры.

## Запуск через Gradle

```bash
gradle runJadeGui
```

или:

```bash
./gradlew runJadeGui
```

## Ожидаемый вывод

```text
playerX is ready. Starter = true
playerO is ready. Starter = false
playerX found opponent: playerO
playerX starts the game as X.
playerX sent move: X -> cell 0
playerO received move from playerX: X -> cell 0
...
game over. Draw.
```

Так как оба агента используют оптимальный выбор хода minimax, при корректной работе игра обычно заканчивается ничьей.

## Проверка через GUI

В окне RMA можно открыть:

```text
Tools → Start Sniffer
```

После запуска Sniffer выберите `playerX` и `playerO`, нажмите правой кнопкой мыши и выберите:

```text
Do sniff this agent(s)
```

Так можно увидеть обмен ACL-сообщениями между агентами.

## Примечание

После завершения партии агенты не удаляются автоматически, чтобы их было видно в GUI. Остановить их можно вручную через RMA.
