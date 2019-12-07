package dao;

import dto.ItemWarehouseDTO;
import entity.ItemWarehouse;
import service.SessionFactoryService;
import org.hibernate.Session;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ItemWarehouseDAO {
    private SessionFactoryService sfService;
    private static final Logger logger = LogManager.getLogger(ItemWarehouseDAO.class);

    public ItemWarehouseDAO(SessionFactoryService sfService) {
        this.sfService = sfService;
    }

    public ItemWarehouse getItemWarehouseByItemId(long id) {
        Session session = null;
        ItemWarehouse itemWarehouse = null;
        try {
            session = sfService.getOpenedSession();
            session.beginTransaction();
            CriteriaQuery<ItemWarehouse> query = session.getCriteriaBuilder().createQuery(ItemWarehouse.class);
            Root<ItemWarehouse> root = query.from(ItemWarehouse.class);
            query.select(root).where(root.get("id").in(id));
            List<ItemWarehouse> results = session.createQuery(query).getResultList();
            itemWarehouse = results.isEmpty() ? null : results.get(0);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            logger.error(e);
        } finally {
            sfService.closeSession(session);
        }
        return itemWarehouse;
    }

    public void save(ItemWarehouse obj) {
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

    public void update(ItemWarehouse obj) {
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
