package co.faxapp.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

import co.faxapp.model.FaxEntity;

public interface IFaxEntityDao extends Dao<FaxEntity, Long> {
    PreparedQuery<FaxEntity> getQuery() throws SQLException, SQLException;

    OrmliteCursorLoader<FaxEntity> getSQLCursorLoader(Context context, PreparedQuery<FaxEntity> query) throws SQLException;
}
