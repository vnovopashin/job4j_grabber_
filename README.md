# job4j_grabber

Агрегатор вакансий — приложение, которое по расписанию собирает вакансии с сайта
и сохраняет их в базу данных, а также отдаёт их через простой web-интерфейс.

## 📖 О проекте

**job4j_grabber** — учебный проект в рамках Job4j. Приложение запускается,
поднимает планировщик задач и web-сервер, а затем по расписанию парсит вакансии
с сайта [career.habr.com](https://career.habr.com) в разделе
[Java-разработчик](https://career.habr.com/vacancies/java_developer).

Сейчас реализовано:

- парсинг **первых 5 страниц** списка вакансий;
- извлечение описания для каждой вакансии с отдельной страницы;
- сохранение вакансий в PostgreSQL;
- периодический запуск парсинга через **Quartz**;
- web-интерфейс на **Javalin**, отдающий список сохранённых вакансий по `GET /`.

## 🎯 Цели расширения

1. **Добавление новых сайтов без изменения кода** — архитектура должна позволять
   подключать новые источники вакансий (реализации `Parse`) через конфигурацию.
2. **Параллельный парсинг сайтов** — возможность одновременного сбора данных
   с нескольких источников.

## 🛠 Стек технологий

- **Java 17**
- **Maven** — сборка и управление зависимостями
- **PostgreSQL** — основное хранилище
- **H2** (in-memory, `test` scope) — для тестов
- **Liquibase** — миграции схемы БД
- **Quartz** — планировщик задач
- **Jsoup** — парсинг HTML
- **Javalin** — web-сервер
- **JUnit 5 + AssertJ** — тесты
- **Log4j / SLF4J (reload4j)** — логирование

## 📋 Требования

- JDK 17 или выше
- Maven 3.8+
- PostgreSQL 12+ (локально или в Docker)

## ⚙️ Конфигурация

Основные настройки хранятся в `src/main/resources/db/liquibase.properties`:

```properties
changeLogFile=src/main/resources/db/dbchangelog.xml
url=jdbc:postgresql://localhost:5432/grabber
username=postgres
password=password
driver-class-name=org.postgresql.Driver
server.port=9000
rabbit.interval=3600
```

Ключи:

| Ключ                                               | Назначение                                                                                   |
|----------------------------------------------------|----------------------------------------------------------------------------------------------|
| `url`, `username`, `password`, `driver-class-name` | Параметры подключения к PostgreSQL. Используются и Liquibase, и приложением (`Config.load`). |
| `changeLogFile`                                    | Путь к главному changelog-файлу Liquibase.                                                   |
| `server.port`                                      | Порт, на котором поднимается Javalin (`Main`).                                               |
| `rabbit.interval`                                  | Период запуска парсинга в **секундах** (Quartz `SimpleSchedule`).                            |

Тестовая конфигурация Liquibase — `src/main/resources/db/liquibase_test.properties`
(используется Maven-профилем `test` по умолчанию, гоняет миграции против H2).

### Профили Maven

- `test` (активен по умолчанию) — Liquibase применяет миграции к H2 `./testdb`.
- `production` — Liquibase применяет миграции к PostgreSQL из `liquibase.properties`.

## 🚀 Запуск

1. Создать БД `grabber` в PostgreSQL.
2. Применить миграции:

```bash
   mvn -Pproduction liquibase:update
```
3. Запустить приложение:

```bash
   mvn -Pproduction exec:java -Dexec.mainClass=ru.job4j.Main
```
Либо собрать и запустить jar:

```bash
mvn -Pproduction package
java -jar target/job4j_grabber_-1.0-SNAPSHOT.jar
```

4. Открыть web-интерфейс: [http://localhost:9000/](http://localhost:9000/).

При старте `Main`:

- открывает соединение с БД;
- поднимает планировщик Quartz и регистрирует задачу `SuperJobGrab`
  с интервалом `rabbit.interval`;
- в отдельном потоке поднимает Javalin на `server.port`;
- остаётся запущенным, пока приложение не остановят (Ctrl+C).

## 🧩 Структура

```text
src/main/java/ru/job4j/
├── Main.java                       — точка входа
├── grabber/
│   ├── model/Post.java             — модель вакансии
│   ├── service/
│   │   ├── Config.java             — загрузка properties
│   │   ├── Parse.java              — интерфейс парсера
│   │   ├── HabrCareerParse.java    — парсер career.habr.com
│   │   ├── SuperJobGrab.java       — Quartz Job: fetch + save
│   │   ├── SchedulerManager.java   — обёртка над Quartz
│   │   └── Web.java                — Javalin web-интерфейс
│   ├── stores/
│   │   ├── Store.java              — интерфейс хранилища
│   │   ├── MemStore.java           — in-memory реализация
│   │   └── JdbcStore.java          — реализация поверх JDBC/PostgreSQL
│   └── utils/
│       ├── DateTimeParser.java     — интерфейс парсера даты
│       └── HabrCareerDateTimeParser.java
└── resources/
    ├── db/
    │   ├── dbchangelog.xml
    │   ├── liquibase.properties
    │   ├── liquibase_test.properties
    │   └── scripts/001_ddl_create_post_table.sql
    └── log4j.properties
```

## 🗄 Схема БД

Таблица post (см. 001_ddl_create_post_table.sql):


```sql
CREATE TABLE post
(
    id          SERIAL PRIMARY KEY,
    title       TEXT,
    link        TEXT UNIQUE,
    description TEXT,
    time        TIMESTAMP WITH TIME ZONE
);
```

Post.time в модели — Long, epoch seconds (UTC).
При сохранении в БД конвертируется в OffsetDateTime (UTC), при чтении — обратно в epoch seconds.

## ✅ Тесты

```bash
  mvn test
```
Покрыто:

- `HabrCareerParseTest` — разбор сохранённой HTML-фикстуры (habr_page.html, habr_vacancy_page.html), извлечение описания, проверка, что time — в секундах;

- `StoreContractTest` — общий контракт Store для MemStore и JdbcStore на H2 in-memory;

- `HabrCareerParseDateTimeTest` — парсер даты HabrCareerDateTimeParser.

Тесты не ходят в сеть и не требуют PostgreSQL.