package hiber;

import hiber.config.AppConfig;
import hiber.model.Car;
import hiber.model.User;
import hiber.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class MainApp {
    public static void main(String[] args) {
        // запуск Spring
        //
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            UserService userService = context.getBean(UserService.class);

            // Очистка таблиц перед началом
            System.out.println("=== Очистка таблиц ===");
            userService.clear();

            userService.add(new User("User1", "Lastname1", "user1@mail.ru", new Car("Toyota", 1)));
            userService.add(new User("User2", "Lastname2", "user2@mail.ru"));
            userService.add(new User("User3", "Lastname3", "user3@mail.ru", new Car("Lada", 2101)));
            userService.add(new User("User4", "Lastname4", "user4@mail.ru", new Car("Citroen", 4)));

            System.out.println("---ВСЕ ПОЛЬЗОВАТЕЛИ---");
            List<User> users = userService.listUsers();
            for (User user : users) {
                System.out.println("Id = " + user.getId());
                System.out.println("First Name = " + user.getFirstName());
                System.out.println("Last Name = " + user.getLastName());
                System.out.println("Email = " + user.getEmail());
                if (user.getCar() != null) {
                    System.out.println("Car = " + user.getCar());
                } else {
                    System.out.println("There is no car");
                }
                System.out.println();
            }

            System.out.println("---ПОИСК ПОЛЬЗОВАТЕЛЯ ПО АВТО---");
            User ladaOwner = userService.getUserByCarModelAndSeries("Lada", 2101);
            if (ladaOwner != null) {
                System.out.println(
                        "Владелец Lada 2101: "
                                + ladaOwner.getFirstName()
                                + " "
                                + ladaOwner.getLastName());
            } else {
                System.out.println("Lada 2101 не найдена.");
            }
        }
        //context.close();
    }
}
