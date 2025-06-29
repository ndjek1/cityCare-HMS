package dao.impl;

import dao.GenericDao;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractDao<T, ID extends Serializable> implements GenericDao<T, ID> {


    private final SessionFactory factory;

    private final Class<T> persistentClass;

    public AbstractDao(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
        this.factory = HibernateUtil.getSessionFactory();
    }

    protected <R> R execute(Function<Session, R> action) {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            R result = action.apply(session);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            System.err.println("Transaction failed for " + persistentClass.getSimpleName() + ". Rolling back.");
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            // In a real application, you'd have a more sophisticated logging and exception handling strategy
            e.printStackTrace();
            throw e;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void save(T entity) {
        execute(session -> {
            session.persist(entity);
            return null;
        });
    }

    @Override
    public void update(T entity) {
        execute(session -> {
            session.merge(entity);
            return null;
        });
    }

    @Override
    public void delete(T entity) {
        execute(session -> {
            session.remove(session.contains(entity) ? entity : session.merge(entity));
            return null;
        });
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) return Optional.empty();
        return execute(session -> Optional.ofNullable(session.get(persistentClass, id)));
    }

    @Override
    public List<T> findAll() {
        return execute(session -> {
            try {
                return session.createQuery("FROM " + persistentClass.getName(), persistentClass).getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }
}