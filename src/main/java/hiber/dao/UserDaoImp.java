package hiber.dao;

import hiber.model.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class UserDaoImp implements UserDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserDaoImp(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void add(User user) {
        sessionFactory.getCurrentSession()
                .save(user);
    }

    @Override
    //@SuppressWarnings("unchecked")
    public List<User> listUsers() {
        /*
         * РЕШАЕМ ПРОБЛЕМУ LAZY-INITIALIZATION И N+1 ЗАПРОСОВ
         *
         * Связь User-Car настроена как LAZY (ленивая загрузка) для оптимизации.
         * Без FETCH:
         * 1. Сначала выполнится: SELECT * FROM users (1 запрос)
         * 2. Затем для каждого пользователя с машиной: SELECT * FROM cars WHERE id = ? (N запросов)
         * ИТОГО: 1 + N запросов (проблема N+1)
         *
         * LEFT JOIN:
         * - Берем ВСЕХ пользователей, даже если у них нет машины (car_id = NULL)
         * - INNER JOIN исключил бы пользователей без машин (например, User2)
         *
         * FETCH (специфика Hibernate):
         * - Заставляет Hibernate загрузить связанную сущность Car ВМЕСТЕ с User
         * - В ОДНОМ запросе: SELECT u.*, c.* FROM users u LEFT JOIN cars c ON u.car_id = c.id
         * - Решает проблему LazyInitializationException при обращении к user.getCar()
         *   после закрытия сессии/транзакции
         *
         * Почему LAZY в аннотации @OneToOne?
         * - По умолчанию: избегаем загрузки лишних данных
         * - В большинстве случаев машина не нужна (например, при поиске по email)
         * - Явно указываем FETCH только там, где машины действительно нужны
         */
        TypedQuery<User> query = sessionFactory.getCurrentSession()
                .createQuery("FROM User u LEFT JOIN FETCH u.car", User.class);
                //createQuery("from User");
        return query.getResultList();
    }

    @Override
    public User getUserByCarModelAndSeries(String model, int series) {
        try {
            TypedQuery<User> query = sessionFactory.getCurrentSession()
                    .createQuery(
                            "FROM User u WHERE u.car.model = :model AND u.car.series = :series",
                            User.class);
            query.setParameter("model", model);
            query.setParameter("series", series);

            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void clear() {
        // Сначала пользователей (из-за внешнего ключа)
        sessionFactory.getCurrentSession()
                .createQuery("DELETE FROM User")
                .executeUpdate();

        // Потом машины
        sessionFactory.getCurrentSession()
                .createQuery("DELETE FROM Car")
                .executeUpdate();


//        // Загружаем всех пользователей через Hibernate
//        List<User> allUsers = listUsers();
//
//        // Удаляем каждого (сработает orphanRemoval!)
//        for (User user : allUsers) {
//            sessionFactory.getCurrentSession().delete(user);
//        }
//
//        // Hibernate автоматически удалит связанные машины
//        // благодаря orphanRemoval = true
    }
}
