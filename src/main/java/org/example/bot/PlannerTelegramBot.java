package org.example.bot;

import org.example.config.BotConfig;
import org.example.dto.TaskCreateRequest;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.model.*;
import org.example.service.CategoryService;
import org.example.service.TaskService;
import org.example.service.UserRegistrationResult;
import org.example.service.UserService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlannerTelegramBot
        implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final BotConfig botConfig;

    private final UserService userService;
    private final TaskService taskService;
    private final CategoryService categoryService;

    private final Map<Long, TaskCreateRequest> taskRequests = new HashMap<>();
    private final Map<Long, String> pendingCategoryNames = new HashMap<>();
    private final Map<Long, Long> pendingStatusTaskIds = new HashMap<>();
    private final Map<Long, Long> pendingAssignTaskIds = new HashMap<>();

    public PlannerTelegramBot(BotConfig botConfig, UserService userService, TaskService taskService, CategoryService categoryService) {
        this.botConfig = botConfig;
        this.userService = userService;
        this.taskService = taskService;
        this.categoryService = categoryService;

        this.telegramClient = new OkHttpTelegramClient(botConfig.getBotToken());
    }

    @Override
    public void consume(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();

        Long telegramId = update.getMessage().getFrom().getId();

        String username = update.getMessage().getFrom().getUserName();

        String firstName = update.getMessage().getFrom().getFirstName();

        String text = update.getMessage().getText();
        if (text.equals("/start")) {
           handleStart(chatId, telegramId, username, firstName);
            return;
        }

        User user = userService.getUserByTelegramId(telegramId);

        if (user == null) {
           sendMessage(chatId, "Пользователь не найден. Введите /start");
            return;
        }

        switch (user.getState()) {

            case IDLE -> handleIdle(chatId, telegramId, user, text);

            case WAITING_FOR_TITLE -> handleTitle(user, chatId, telegramId, text);

            case WAITING_FOR_DESCRIPTION -> handleDescription(user, chatId, telegramId, text);

            case WAITING_FOR_DEADLINE -> handleDeadline(user, chatId, telegramId, text);

            case WAITING_FOR_PRIORITY -> handlePriority(user, chatId, telegramId, text);

            case WAITING_FOR_CATEGORY -> handleCategory(user, chatId, telegramId, text);

            case WAITING_FOR_NEW_CATEGORY_NAME -> handleNewCategoryName(user, chatId, telegramId, text);

            case WAITING_FOR_NEW_CATEGORY_COLOR -> handleNewCategoryColor(user, chatId, telegramId, text);

            case WAITING_FOR_DELETE_TASK_ID -> handleDeleteTaskId(user, chatId, text);

            case WAITING_FOR_STATUS_TASK_ID -> handleStatusTaskId(user, chatId, telegramId, text);

            case WAITING_FOR_NEW_STATUS -> handleNewStatus(user, chatId, telegramId, text);

            case WAITING_FOR_STANDALONE_CATEGORY_NAME -> handleStandaloneCategoryName(user, chatId, telegramId, text);

            case WAITING_FOR_STANDALONE_CATEGORY_COLOR -> handleStandaloneCategoryColor(user, chatId, telegramId, text);

            case WAITING_FOR_DELETE_CATEGORY_ID -> handleDeleteCategoryId(user, chatId, text);

            case WAITING_FOR_CATEGORY_CHOICE -> handleCategoryChoice(user, chatId, telegramId, text);

            case WAITING_FOR_ASSIGN_TASK_ID -> handleAssignTaskId(user, chatId, telegramId, text);

            case WAITING_FOR_ASSIGN_CATEGORY_CHOICE -> handleAssignCategoryChoice(user, chatId, telegramId, text);

            case WAITING_FOR_ASSIGN_CATEGORY_ID -> handleAssignCategoryId(user, chatId, telegramId, text);

            case WAITING_FOR_ASSIGN_NEW_CATEGORY_NAME -> handleAssignNewCategoryName(user, chatId, telegramId, text);

            case WAITING_FOR_ASSIGN_NEW_CATEGORY_COLOR -> handleAssignNewCategoryColor(user, chatId, telegramId, text);
        }
    }

    public void handleStart(Long chatId, Long telegramId, String username, String firstName) {

        UserRegistrationResult result = userService.getOrCreateUser(telegramId, username, firstName);

        User user = result.user();
        user.setState(UserState.IDLE);
        userService.update(user);

        if (result.isNew()) {
            sendMessage(chatId, "Добро пожаловать, " + user.getFirstName() + "!");
        } else {
            sendMessage(chatId, "С возвращением, " + user.getFirstName() + "!");
        }
    }


    public void handleIdle(Long chatId, Long telegramId, User user, String text) {

        switch (text) {
            case "📋 Мои задачи" -> handleShowTasks(chatId, user);
            case "➕ Создать задачу" -> handleCreateTask(chatId, telegramId);
            case "📂 Категории" -> handleCategories(chatId);
            case "🔙 Главное меню" -> sendMessage(chatId, "Главное меню:", createMainMenu());
            case "🗑 Удалить задачу" -> {
                user.setState(UserState.WAITING_FOR_DELETE_TASK_ID);
                userService.update(user);
                sendMessage(chatId, "Введите ID задачи для удаления:");
            }
            case "🔄 Поменять статус задачи" -> {
                user.setState(UserState.WAITING_FOR_STATUS_TASK_ID);
                userService.update(user);
                sendMessage(chatId, "Введите ID задачи для смены статуса:");
            }

            case "➕ Создать категорию" -> {
                user.setState(UserState.WAITING_FOR_STANDALONE_CATEGORY_NAME);
                userService.update(user);
                sendMessage(chatId, "Введите название новой категории:");
            }

            case "📂 Удалить категорию" -> {
                user.setState(UserState.WAITING_FOR_DELETE_CATEGORY_ID);
                userService.update(user);
                sendMessage(chatId, "Введите ID категории для удаления");
            }

            case "🏷 Назначить категорию" -> {
                user.setState(UserState.WAITING_FOR_ASSIGN_TASK_ID);
                userService.update(user);
                sendMessage(chatId, "Введите ID задачи, которой хотите назначить категорию:");
            }

            default -> sendMessage(chatId, "Выберите действие из меню.");
        }
    }

    private void handleCreateTask(Long chatId, Long telegramId) {

        TaskCreateRequest request = new TaskCreateRequest();

        taskRequests.put(telegramId, request);
        User user = userService.getUserByTelegramId(telegramId);
        user.setState(UserState.WAITING_FOR_TITLE);
        userService.update(user);

        sendMessage(chatId, "Введите название задачи:");
    }

    private void handleShowTasks(Long chatId, User user) {

        List<Task> taskList = taskService.showAll(user);

        if (taskList.isEmpty()) {
            sendMessage(chatId, "Список задач пока пуст.");
            return;
        }

        StringBuilder message = new StringBuilder("📋 Ваши задачи:\n\n");
        for (Task task : taskList) {
            message.append(task).append("\n\n");
        }
        sendMessage(chatId, message.toString(), createTaskMenu());
    }

    /*
     * =========================
     * TITLE
     * =========================
     */

    private void handleTitle(User user, Long chatId, Long telegramId, String text) {

        TaskCreateRequest request = taskRequests.get(telegramId);

        if (request == null) {
            sendMessage(chatId, "Начните создание задачи заново.");

            user.setState(UserState.IDLE);
            userService.update(user);
            return;
        }

        request.setTitle(text);

        user.setState(UserState.WAITING_FOR_DESCRIPTION);
        userService.update(user);

        sendMessage(chatId, "Введите описание задачи:");
    }

    /*
     * =========================
     * DESCRIPTION
     * =========================
     */

    private void handleDescription(User user, Long chatId, Long telegramId, String text) {

        TaskCreateRequest request = taskRequests.get(telegramId);

        if (request == null) {
            sendMessage(chatId, "Начните создание задачи заново.");

            user.setState(UserState.IDLE);
            userService.update(user);

            return;
        }

        request.setDescription(text);

        user.setState(UserState.WAITING_FOR_DEADLINE);
        userService.update(user);

        sendMessage(chatId, "Введите срок задачи.\n\n" + "Формат:\n" + "31.08.2026 18:30");
    }

    /*
     * =========================
     * DEADLINE
     * =========================
     */

    private void handleDeadline(User user, Long chatId, Long telegramId, String text) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        try {
            LocalDateTime dateTime = LocalDateTime.parse(text, formatter);

            Instant deadline = dateTime.atZone(ZoneId.systemDefault()).toInstant();

            TaskCreateRequest request = taskRequests.get(telegramId);

            if (request == null) {

                sendMessage(chatId, "Начните создание задачи заново.");

                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            request.setDeadline(deadline);

            user.setState(UserState.WAITING_FOR_PRIORITY);
            userService.update(user);
            sendMessage(chatId, "Выберите приоритет:", createPriorityMenu());

        } catch (DateTimeParseException e) {

            sendMessage(chatId, "❌ Неверный формат даты.\n\n" + "Используйте:\n" + "31.08.2026 18:30"
            );
        }
    }

    /*
     * =========================
     * PRIORITY
     * =========================
     */

    private void handlePriority(User user, Long chatId, Long telegramId, String text) {
        try {
            PriorityValues priority = PriorityValues.valueOf(text.toUpperCase());

            TaskCreateRequest request = taskRequests.get(telegramId);

            if (request == null) {
                sendMessage(chatId, "Начните создание задачи заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            request.setPriority(priority);

            user.setState(UserState.WAITING_FOR_CATEGORY_CHOICE);
            userService.update(user);

            sendMessage(chatId, "Привязать категорию к задаче?", createCategoryChoiceMenu());

        } catch (IllegalArgumentException e) {
            sendMessage(chatId," ❌ Неверный приоритет Выберите один из вариантов на клавиатуре.");
        }
    }



    /*
     * =========================
     * CATEGORY
     * =========================
     */

    private void handleNewCategoryName(User user, Long chatId, Long telegramId, String text) {

        pendingCategoryNames.put(telegramId, text);

        user.setState(UserState.WAITING_FOR_NEW_CATEGORY_COLOR);
        userService.update(user);

        sendMessage(chatId, "Выберите цвет категории:", createColorMenu());
    }

    private void handleNewCategoryColor(User user, Long chatId, Long telegramId, String text) {
        try {
            CategoryCollors color = CategoryCollors.valueOf(text.toUpperCase());

            String name = pendingCategoryNames.get(telegramId);

            if (name == null) {
                sendMessage(chatId, "Начните создание категории заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            Category category = categoryService.create(name, color);
            pendingCategoryNames.remove(telegramId);

            TaskCreateRequest request = taskRequests.get(telegramId);

            if (request == null) {
                sendMessage(chatId, "Начните создание задачи заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            request.setCategory(category);

            taskService.create(request, user);
            taskRequests.remove(telegramId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Категория «" + category.getName() + "» создана, задача успешно добавлена!");

        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "❌ Такого цвета нет. Выберите один из предложенных вариантов.");
        }
    }

    private void handleCategory(User user, Long chatId, Long telegramId, String text) {
        try {
            Long categoryId = Long.parseLong(text);

            var category = categoryService.getById(categoryId);

            if (category == null) {
                sendMessage(chatId, "❌ Категория с таким ID не найдена.");
                return;
            }

            TaskCreateRequest request = taskRequests.get(telegramId);

            if (request == null) {

                sendMessage(chatId, "Начните создание задачи заново.");

                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            request.setCategory(category);
            taskService.create(request, user);
            taskRequests.remove(telegramId);
            user.setState(UserState.IDLE);
            userService.update(user);
            sendMessage(chatId, "✅ Задача успешно создана!");

        } catch (NumberFormatException e) {

            sendMessage(chatId, "❌ Введите числовой ID категории.");
        }
    }

    private void handleCategoryChoice(User user, Long chatId, Long telegramId, String text) {

        TaskCreateRequest request = taskRequests.get(telegramId);

        if (request == null) {
            sendMessage(chatId, "Начните создание задачи заново.");
            user.setState(UserState.IDLE);
            userService.update(user);
            return;
        }

        switch (text) {
            case "📂 Выбрать существующую" -> {
                List<Category> categories = categoryService.getAll();

                if (categories.isEmpty()) {
                    sendMessage(chatId, "У вас пока нет ни одной категории. Выберите другой вариант:", createCategoryChoiceMenu());
                    return;
                }

                StringBuilder message = new StringBuilder("Выберите ID категории:\n\n");
                for (Category category : categories) {
                    message.append(category.getId())
                            .append(" — ")
                            .append(category.getName())
                            .append("\n");
                }
                sendMessage(chatId, message.toString());

                user.setState(UserState.WAITING_FOR_CATEGORY);
                userService.update(user);
            }

            case "➕ Создать новую" -> {
                sendMessage(chatId, "Введите название новой категории:");
                user.setState(UserState.WAITING_FOR_NEW_CATEGORY_NAME);
                userService.update(user);
            }

            case "🚫 Без категории" -> {
                request.setCategory(null);
                taskService.create(request, user);
                taskRequests.remove(telegramId);

                user.setState(UserState.IDLE);
                userService.update(user);

                sendMessage(chatId, "✅ Задача успешно создана без категории!", createMainMenu());
            }

            default -> sendMessage(chatId, "Выберите один из вариантов на клавиатуре.", createCategoryChoiceMenu());
        }
    }

    /*
     * =========================
     * CATEGORIES
     * =========================
     */

    private void handleCategories(Long chatId) {

        List<Category> categories = categoryService.getAll();

        StringBuilder message = new StringBuilder("📂 Ваши категории:\n\n");

        if (categories.isEmpty()) {
            message.append("Пока нет ни одной категории.\n");
        } else {
            for (Category category : categories) {
                message.append(category).append("\n");
            }
        }

        sendMessage(chatId, message.toString(), createCategoryMenu());
    }

    private void handleDeleteTaskId(User user, Long chatId, String text) {
        try {
            Long taskId = Long.parseLong(text);

            taskService.delete(taskId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Задача удалена.", createTaskMenu());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Введите числовой ID задачи.");
        } catch (RuntimeException e) {
            sendMessage(chatId, "❌ Задача с таким ID не найдена.");
        }
    }

    /*
     * =========================
     * STANDALONE CATEGORY CREATE
     * =========================
     */

    private void handleStandaloneCategoryName(User user, Long chatId, Long telegramId, String text) {

        pendingCategoryNames.put(telegramId, text);

        user.setState(UserState.WAITING_FOR_STANDALONE_CATEGORY_COLOR);
        userService.update(user);

        sendMessage(chatId, "Выберите цвет категории:", createColorMenu());
    }

    private void handleStandaloneCategoryColor(User user, Long chatId, Long telegramId, String text) {
        try {
            CategoryCollors color = CategoryCollors.valueOf(text.toUpperCase());

            String name = pendingCategoryNames.get(telegramId);

            if (name == null) {
                sendMessage(chatId, "Начните создание категории заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            Category category = categoryService.create(name, color);
            pendingCategoryNames.remove(telegramId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Категория «" + category.getName() + "» создана!", createCategoryMenu());

        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "❌ Такого цвета нет. Выберите один из предложенных вариантов.");
        }
    }

    /*
     * =========================
     * DELETE CATEGORY
     * =========================
     */

    private void handleDeleteCategoryId(User user, Long chatId, String text) {
        try {
            Long categoryId = Long.parseLong(text);

            categoryService.delete(categoryId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Категория удалена.", createCategoryMenu());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Введите числовой ID категории.");
        }
    }
    /*
     * =========================
     * CHANGE STATUS
     * =========================
     */

    private void handleStatusTaskId(User user, Long chatId, Long telegramId, String text) {
        try {
            Long taskId = Long.parseLong(text);

            pendingStatusTaskIds.put(telegramId, taskId);   // сохраняем ID до выбора статуса

            user.setState(UserState.WAITING_FOR_NEW_STATUS);
            userService.update(user);

            sendMessage(chatId, "Выберите новый статус:", createStatusMenu());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Введите числовой ID задачи.");
        }
    }

    private void handleNewStatus(User user, Long chatId, Long telegramId, String text) {
        try {
            TaskStatus status = TaskStatus.valueOf(text.toUpperCase());

            Long taskId = pendingStatusTaskIds.get(telegramId);

            if (taskId == null) {
                sendMessage(chatId, "Начните смену статуса заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            taskService.changeStatus(taskId, status);   // ⬅️ подставь своё реальное название метода, если отличается
            pendingStatusTaskIds.remove(telegramId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Статус обновлён.", createTaskMenu());

        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "❌ Такого статуса нет. Выберите один из предложенных вариантов.");
        } catch (RuntimeException e) {
            sendMessage(chatId, "❌ Задача с таким ID не найдена.");
        }
    }

    /*
     * =========================
     * SEND MESSAGE
     * =========================
     */

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, createMainMenu());
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboardMarkup markup) {

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        message.setReplyMarkup(markup);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка отправки сообщения", e);
        }
    }

    /*
     * =========================
     * ASSIGN CATEGORY TO EXISTING TASK
     * =========================
     */

    private void handleAssignTaskId(User user, Long chatId, Long telegramId, String text) {
        try {
            Long taskId = Long.parseLong(text);

            pendingAssignTaskIds.put(telegramId, taskId);

            user.setState(UserState.WAITING_FOR_ASSIGN_CATEGORY_CHOICE);
            userService.update(user);

            sendMessage(chatId, "Какую категорию назначить?", createCategoryChoiceMenu());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Введите числовой ID задачи.");
        }
    }

    private void handleAssignCategoryChoice(User user, Long chatId, Long telegramId, String text) {

        Long taskId = pendingAssignTaskIds.get(telegramId);

        if (taskId == null) {
            sendMessage(chatId, "Начните назначение категории заново.");
            user.setState(UserState.IDLE);
            userService.update(user);
            return;
        }

        switch (text) {
            case "📂 Выбрать существующую" -> {
                List<Category> categories = categoryService.getAll();

                if (categories.isEmpty()) {
                    sendMessage(chatId, "У вас пока нет ни одной категории. Выберите другой вариант:", createCategoryChoiceMenu());
                    return;
                }

                StringBuilder message = new StringBuilder("Выберите ID категории:\n\n");
                for (Category category : categories) {
                    message.append(category.getId())
                            .append(" — ")
                            .append(category.getName())
                            .append("\n");
                }
                sendMessage(chatId, message.toString());

                user.setState(UserState.WAITING_FOR_ASSIGN_CATEGORY_ID);
                userService.update(user);
            }

            case "➕ Создать новую" -> {
                sendMessage(chatId, "Введите название новой категории:");
                user.setState(UserState.WAITING_FOR_ASSIGN_NEW_CATEGORY_NAME);
                userService.update(user);
            }

            case "🚫 Без категории" -> {
                taskService.setCategory(taskId, null);
                pendingAssignTaskIds.remove(telegramId);

                user.setState(UserState.IDLE);
                userService.update(user);

                sendMessage(chatId, "✅ Категория у задачи убрана.", createTaskMenu());
            }

            default -> sendMessage(chatId, "Выберите один из вариантов на клавиатуре.", createCategoryChoiceMenu());
        }
    }

    private void handleAssignCategoryId(User user, Long chatId, Long telegramId, String text) {
        try {
            Long categoryId = Long.parseLong(text);

            Category category = categoryService.getById(categoryId);

            Long taskId = pendingAssignTaskIds.get(telegramId);

            if (taskId == null) {
                sendMessage(chatId, "Начните назначение категории заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            taskService.setCategory(taskId, category);
            pendingAssignTaskIds.remove(telegramId);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Категория «" + category.getName() + "» назначена задаче!", createTaskMenu());

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Введите числовой ID категории.");
        } catch (RuntimeException e) {
            sendMessage(chatId, "❌ Категория с таким ID не найдена.");
        }
    }

    private void handleAssignNewCategoryName(User user, Long chatId, Long telegramId, String text) {

        pendingCategoryNames.put(telegramId, text);

        user.setState(UserState.WAITING_FOR_ASSIGN_NEW_CATEGORY_COLOR);
        userService.update(user);

        sendMessage(chatId, "Выберите цвет категории:", createColorMenu());
    }

    private void handleAssignNewCategoryColor(User user, Long chatId, Long telegramId, String text) {
        try {
            CategoryCollors color = CategoryCollors.valueOf(text.toUpperCase());

            String name = pendingCategoryNames.get(telegramId);
            Long taskId = pendingAssignTaskIds.get(telegramId);

            if (name == null || taskId == null) {
                sendMessage(chatId, "Начните назначение категории заново.");
                user.setState(UserState.IDLE);
                userService.update(user);
                return;
            }

            Category category = categoryService.create(name, color);
            pendingCategoryNames.remove(telegramId);
            pendingAssignTaskIds.remove(telegramId);

            taskService.setCategory(taskId, category);

            user.setState(UserState.IDLE);
            userService.update(user);

            sendMessage(chatId, "✅ Категория «" + category.getName() + "» создана и назначена задаче!", createTaskMenu());

        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "❌ Такого цвета нет. Выберите один из предложенных вариантов.");
        }
    }


    /*
     * =========================
     * MAIN MENU
     * =========================
     */

    private ReplyKeyboardMarkup createMainMenu() {

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("📋 Мои задачи");
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("➕ Создать задачу");
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("📂 Категории");
        return ReplyKeyboardMarkup
                .builder()
                .keyboard(
                        List.of(firstRow, secondRow, thirdRow)
                )
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createTaskMenu() {
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("🔄 Поменять статус задачи");
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("🗑 Удалить задачу");
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("🏷 Назначить категорию");
        KeyboardRow fourthRow = new KeyboardRow();
        fourthRow.add("🔙 Главное меню");
        return ReplyKeyboardMarkup
                .builder()
                .keyboard(
                        List.of(firstRow, secondRow, thirdRow)
                )
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createCategoryChoiceMenu() {

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("📂 Выбрать существующую");
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("➕ Создать новую");
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("🚫 Без категории");

        return ReplyKeyboardMarkup
                .builder()
                .keyboard(List.of(firstRow, secondRow, thirdRow))
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createStatusMenu() {

        KeyboardRow row = new KeyboardRow();
        for (TaskStatus status : TaskStatus.values()) {
            row.add(status.name());
        }

        return ReplyKeyboardMarkup
                .builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createPriorityMenu() {

        KeyboardRow row = new KeyboardRow();
        for (PriorityValues priority : PriorityValues.values()) {
            row.add(priority.name());
        }

        return ReplyKeyboardMarkup
                .builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createCategoryMenu() {

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("📋 Мои категории");
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("➕ Создать категорию");
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("📂 Удалить категорию");
        KeyboardRow fourthRow = new KeyboardRow();
        fourthRow.add("🔙 Главное меню");
        return ReplyKeyboardMarkup
                .builder()
                .keyboard(
                        List.of(firstRow, secondRow, thirdRow, fourthRow)
                )
                .resizeKeyboard(true)
                .build();
    }

    private ReplyKeyboardMarkup createColorMenu() {

        KeyboardRow row = new KeyboardRow();
        for (CategoryCollors color : CategoryCollors.values()) {
            row.add(color.name());
        }

        return ReplyKeyboardMarkup
                .builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }
}