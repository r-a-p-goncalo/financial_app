package com.rgoncalo.financialapp.infrastructure.persistence.sqlite.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface SQLiteRecordMapper<T> {

    String tableName();

    List<ColumnDefinition> toColumns(T record);

    T fromResultSet(ResultSet resultSet) throws SQLException;

}