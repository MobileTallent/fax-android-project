package co.faxapp.db;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

import co.faxapp.model.FaxEntity;

public class FaxEntityDao extends AndroidBaseDaoImpl<FaxEntity, Long> implements IFaxEntityDao {
    public FaxEntityDao(ConnectionSource connectionSource, Class<FaxEntity> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

//    public FaxEntity getEdoMessageByMessageIdentity(int documentId, String userEmail) throws SQLException {
//        QueryBuilder<FaxEntity, Long> queryBuilder = queryBuilder();
//        queryBuilder.where()
//                .eq("userEmail", userEmail).and()
//                .eq("systemId", documentId);
//        PreparedQuery<FaxEntity> preparedQuery = queryBuilder.prepare();
//        queryForFirst(preparedQuery);
//        return queryForFirst(preparedQuery);
//    }


    public List<FaxEntity> getInProgressList() throws SQLException {
        QueryBuilder<FaxEntity, Long> queryBuilder = queryBuilder();
        queryBuilder.where().eq("status", 1);
        PreparedQuery<FaxEntity> preparedQuery = queryBuilder.prepare();
        return query(preparedQuery);
    }


    public PreparedQuery<FaxEntity> getQuery() throws SQLException {
        return queryBuilder().orderBy("updateDate", false).prepare();
    }

//    public PreparedQuery<FaxEntity> getQuery(boolean inDoc, int status, String userEmail) throws SQLException {
//        QueryBuilder<FaxEntity, Long> queryBuilder = queryBuilder();
//        queryBuilder.orderBy("updateDate", false);
//        if (status == -1) {
//            return queryBuilder.where()
//                    .eq("inDocument", inDoc).and()
//                    .eq("userEmail", userEmail).prepare();
//        } else {
//            return queryBuilder.where()
//                    .eq("userEmail", userEmail).and()
//                    .eq("inDocument", inDoc).and()
//                    .eq("statusId", status)
//                    .prepare();
//        }
//    }
}
