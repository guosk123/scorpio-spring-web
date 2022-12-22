package com.machloop.fpc.manager.statistics.dao.postgres;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.statistics.dao.TimeseriesDao;
import com.machloop.fpc.manager.statistics.data.TimeseriesDO;
import com.machloop.fpc.manager.statistics.service.RrdService;

/**
 * @author liumeng
 *
 * create at 2018年12月21日, fpc-manager
 */
@Repository
public class TimeseriesDaoImpl implements TimeseriesDao {

  private static final String TABLE_STATISTICS_TIMESERIES = "fpc_statistics_timeseries";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#queryTimeseries(java.lang.String, int)
   */
  @Override
  public TimeseriesDO queryTimeseries(String rrdName, int cellNum) {
    StringBuilder sql = buildSelectStatement();
    appendWhereRrdNameAndCellNum(sql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);
    params.put("cellNum", cellNum);

    List<TimeseriesDO> timeseriesList = jdbcTemplate.query(sql.toString(), params,
        new TimeseriesMapper());

    TimeseriesDO timeseries;
    if (CollectionUtils.isEmpty(timeseriesList)) {
      timeseries = saveEmptyTimeseries(rrdName, cellNum);
    } else {
      timeseries = timeseriesList.get(0);
    }
    return timeseries;
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#queryTimeseries(
   *                                                  java.lang.String, java.util.List)
   */
  @Override
  public List<TimeseriesDO> queryTimeseries(String rrdName, List<Integer> cellNumList) {
    StringBuilder sql = buildSelectStatement()
        .append(" where rrd_name = :rrdName and cell_number in (:cellNumList) ")
        .append(" order by cell_number ASC ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);
    params.put("cellNumList", cellNumList);

    return jdbcTemplate.query(sql.toString(), params, new TimeseriesMapper());
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#queryTimeseries(java.lang.String, int, int)
   */
  @Override
  public long queryTimeseries(String rrdName, int cellNum, int position) {
    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE)
        .append("select data_point[").append(position).append("] from ")
        .append(TABLE_STATISTICS_TIMESERIES);
    appendWhereRrdNameAndCellNum(sql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);
    params.put("cellNum", cellNum);
    params.put("position", position);

    List<Double> valueList = jdbcTemplate.queryForList(sql.toString(), params, Double.class);
    if (CollectionUtils.isEmpty(valueList)) {
      saveEmptyTimeseries(rrdName, cellNum);
      return 0L;
    } else {
      return valueList.get(0) == null ? 0L : valueList.get(0).longValue();
    }
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#saveEmptyTimeseries(
   *                                                      java.lang.String, int)
   */
  @Override
  public TimeseriesDO saveEmptyTimeseries(String rrdName, int cellNum) {
    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("INSERT INTO ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" (id, rrd_name, cell_number, data_point) ")
        .append(" VALUES ( :id, :rrdName, :cellNumber, '{}' ) ");

    TimeseriesDO timeseries = new TimeseriesDO();
    timeseries.setId(IdGenerator.generateUUID());
    timeseries.setRrdName(rrdName);
    timeseries.setCellNumber(cellNum);
    timeseries.setDataPoint(new double[RrdService.POINT_SIZE]);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(timeseries);
    jdbcTemplate.update(sql.toString(), paramSource);
    return timeseries;
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#updateTimeseries(
   *                                    com.machloop.fpc.manager.statistics.data.TimeseriesDO)
   */
  @Override
  public int updateTimeseries(TimeseriesDO timeseriesDO) {

    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("update ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" set data_point = ? ")
        .append(" where rrd_name = ? and cell_number = ? ");

    return jdbcTemplate.getJdbcTemplate().update(new PreparedStatementCreator() {

      @Override
      public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {

        PreparedStatement ps = conn.prepareStatement(sql.toString()); // NOSONAR
                                                                      // ps在回调外层被finally关闭，此处不能使用try-with-resource写法

        double[] dataPoint = timeseriesDO.getDataPoint();
        Array array = conn.createArrayOf("float8", Doubles.asList(dataPoint).toArray());

        ps.setArray(1, array);
        ps.setString(2, timeseriesDO.getRrdName());
        ps.setInt(3, timeseriesDO.getCellNumber());
        return ps;
      }
    });
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#updateTimeseriesDataPoint(
   *                                                            java.lang.String, int, int, long)
   */
  @Override
  public int updateTimeseriesDataPoint(String rrdName, int cellNum, int posInCell, long value) {

    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("update ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" set data_point[" + posInCell + "] = :value ");
    appendWhereRrdNameAndCellNum(sql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);
    params.put("cellNum", cellNum);
    params.put("value", value);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#clearTimeseries(java.lang.String)
   */
  @Override
  public int clearTimeseries(String rrdName) {
    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("update ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" set data_point = '{}' ")
        .append(" where rrd_name = :rrdName ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#clearTimeseries(java.lang.String, int)
   */
  @Override
  public int clearTimeseries(String rrdName, int cellNum) {
    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("update ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" set data_point = '{}' ");
    appendWhereRrdNameAndCellNum(sql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);
    params.put("cellNum", cellNum);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.TimeseriesDao#deleteTimeseries(java.lang.String)
   */
  @Override
  public int deleteTimeseries(String rrdName) {
    StringBuilder sql = new StringBuilder(Constants.BUFFER_DEFAULT_SIZE).append("delete from ")
        .append(TABLE_STATISTICS_TIMESERIES).append(" where rrd_name = :rrdName ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("rrdName", rrdName);

    return jdbcTemplate.update(sql.toString(), params);
  }


  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    return new StringBuilder(Constants.BUFFER_DEFAULT_SIZE)
        .append("select id, rrd_name, cell_number, data_point ").append(" from ")
        .append(TABLE_STATISTICS_TIMESERIES);
  }


  /**
   * @param sql
   */
  private static void appendWhereRrdNameAndCellNum(StringBuilder sql) {
    sql.append(" where rrd_name = :rrdName and cell_number = :cellNum ");
  }

  private class TimeseriesMapper implements RowMapper<TimeseriesDO> {

    @Override
    public TimeseriesDO mapRow(ResultSet rs, int rowNum) throws SQLException {

      TimeseriesDO timeseries = new TimeseriesDO();

      timeseries.setId(rs.getString("id"));
      timeseries.setRrdName(rs.getString("rrd_name"));
      timeseries.setCellNumber(rs.getInt("cell_number"));
      Double[] dataPointArray = ((Double[]) rs.getArray("data_point").getArray());
      double[] points = new double[RrdService.POINT_SIZE];
      if (dataPointArray != null && dataPointArray.length > 0) {
        for (int i = 0; i < dataPointArray.length; i++) {
          if (dataPointArray[i] == null) {
            points[i] = 0D;
          } else {
            points[i] = dataPointArray[i].doubleValue();
          }
        }
      }
      timeseries.setDataPoint(points);

      return timeseries;
    }

  }
}
