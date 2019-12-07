package dao;

import dto.ItemDTO;
import entity.Item;
import service.SessionFactoryService;
import org.hibernate.Session;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ItemDAO {
    private SessionFactoryService sfService;
    private static final Logger logger = LogManager.getLogger(ItemDAO.class);

    public ItemDAO(SessionFactoryService sfService) {
        this.sfService = sfService;
    }

    public List<Item> getItems() {
        Session session = null;
        List<Item> items = null;
        try {
            session = sfService.getOpenedSession();
            session.beginTransaction();
            CriteriaQuery<Item> query = session.getCriteriaBuilder().createQuery(Item.class);
            query.from(Item.class);
            items = session.createQuery(query).getResultList();
            session.getTransaction().commit();
        }
        catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            logger.error(e);
        }
        finally {
            sfService.closeSession(session);
        }
        return items;
    }

    public Item getItemById(long id) {
        Session session = null;
        Item item = null;
        try {
            session = sfService.getOpenedSession();
            session.beginTransaction();
            CriteriaQuery<Item> query = session.getCriteriaBuilder().createQuery(Item.class);
            Root<Item> root = query.from(Item.class);
            query.select(root).where(root.get("id").in(id));
            List<Item> results = session.createQuery(query).getResultList();
            item = results.isEmpty() ? null : results.get(0);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            logger.error(e);
        } finally {
            sfService.closeSession(session);
        }
        return item;
    }

    public void save(Item obj) {
        Session session = null;
        try {
            session = sfService.getOpenedSession();
            session.beginTransaction();
            session.save(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            logger.error(e);
        } finally {
            sfService.closeSession(session);
        }
    }

    public void update(Item obj) {
        Session session = null;
        try {
            session = sfService.getOpenedSession();
            session.beginTransaction();
            session.update(obj);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            logger.error(e);
        } finally {
            sfService.closeSession(session);
        }
    }
}
