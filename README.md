StudyPlannerBot 📋

https://t.me/studplanBot
Telegram-бот для планирования задач с категориями, приоритетами и дедлайнами. Пет-проект для практики Java, Hibernate и работы с Telegram Bot API.

Возможности
Создание задач с названием, описанием, дедлайном и приоритетом
Категории задач (создание, удаление, привязка к задачам)
Изменение статуса задачи (Not Started / In Progress / Done)
Удаление задач

Управление через reply-клавиатуры (без ручного ввода команд)

Стек
Java 21
Hibernate ORM 7
H2 Database
Telegram Bots Java library (long polling)
Maven

Структура проекта
src/main/java/org/example/
├── bot/           # логика Telegram-бота
├── model/         # JPA-сущности (Task, Category, User)
├── Repository/    # доступ к данным
├── service/       # бизнес-логика
├── dto/           # объекты передачи данных
└── config/        # конфигурация бота
