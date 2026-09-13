package org.example;

import org.example.Repository.CategoryRepository;
import org.example.Repository.TaskRepository;
import org.example.Repository.UserRepository;
import org.example.bot.PlannerTelegramBot;
import org.example.config.BotConfig;
import org.example.service.CategoryService;
import org.example.service.TaskService;
import org.example.service.UserService;
import org.hibernate.SessionFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.github.cdimascio.dotenv.Dotenv;
public class Main {
    static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("BOT_TOKEN");
        BotConfig config = new BotConfig(token, "StudyPlannerBot");

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository, sessionFactory);

        TaskRepository taskRepository = new TaskRepository();
        CategoryRepository categoryRepository = new CategoryRepository();
        CategoryService categoryService = new CategoryService(categoryRepository, sessionFactory);

        TaskService taskService = new TaskService(taskRepository, userService, categoryService, sessionFactory);
        PlannerTelegramBot bot =
                new PlannerTelegramBot(config, userService, taskService,categoryService);
        TelegramBotsLongPollingApplication app =
                new TelegramBotsLongPollingApplication();
        try {
            app.registerBot(
                    config.getBotToken(),
                    bot
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}
