package com.machloop.fpc.cms.center.central.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.central.dao.CentralDiskDao;
import com.machloop.fpc.cms.center.central.data.CentralDiskDO;

@Repository
public class CentralDiskDaoImpl implements CentralDiskDao {

  private static final String TABLE_DEVICE_DISK = "fpccms_central_device_disk";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralDiskDao#queryCentralDisks(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<CentralDiskDO> queryCentralDisks(String deviceType, String deviceSerialNumber,
      String raidNo) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where 1=1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(deviceType)) {
      sql.append(" and device_type = :deviceType ");
      params.put("deviceType", deviceType);
    }
    if (StringUtils.isNotBlank(deviceSerialNumber)) {
      sql.append(" and device_serial_number = :deviceSerialNumber ");
      params.put("deviceSerialNumber", deviceSerialNumber);
    }
    if (StringUtils.isNotBlank(raidNo)) {
      sql.append(" and raid_no = :raidNo ");
      params.put("raidNo", raidNo);
    }

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralDiskDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralDiskDao#countCentralDiskByState()
   */
  @Override
  public List<Map<String, Object>> countCentralDiskByState() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select state, COUNT(id) from ");
    sql.append(TABLE_DEVICE_DISK);
    sql.append(" group by state ");

    return jdbcTemplate.queryForList(sql.toString(),
        Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralDiskDao#batchSaveOrUpdateCentralDisks(java.util.List)
   */
  @Override
  public void batchSaveOrUpdateCentralDisks(List<CentralDiskDO> diskList) {
    // delete all disk msg
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_DEVICE_DISK);
    sql.append(" where device_type = :deviceType and device_serial_number = :deviceSerialNumber ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", diskList.get(0).getDeviceType());
    params.put("deviceSerialNumber", diskList.get(0).getDeviceSerialNumber());

    jdbcTemplate.update(sql.toString(), params);

    // batch insert disk msg
    sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_DEVICE_DISK);
    sql.append(" (id, device_type, device_serial_number, physical_location, slot_no, ");
    sql.append(" raid_no, raid_level, state, medium, capacity, ");
    sql.append(" rebuild_progress, copyback_progress, foreign_state, description)");
    sql.append(" values (:id, :deviceType, :deviceSerialNumber, :physicalLocation, :slotNo, ");
    sql.append(" :raidNo, :raidLevel, :state, :medium, :capacity, ");
    sql.append(" :rebuildProgress, :copybackProgress, :foreignState, :description)");

    for (CentralDiskDO fpcDiskDO : diskList) {
      fpcDiskDO.setId(IdGenerator.generateUUID());
    }
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(diskList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralDiskDao#deleteCentralDisks(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralDisks(String deviceType, String deviceSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_DEVICE_DISK);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", deviceSerialNumber);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, device_serial_number, physical_location, slot_no, ");
    sql.append(" raid_no, raid_level, state, medium, capacity, rebuild_progress, ");
    sql.append(" copyback_progress, foreign_state, description ");
    sql.append(" from ").append(TABLE_DEVICE_DISK);
    return sql;
  }

}
