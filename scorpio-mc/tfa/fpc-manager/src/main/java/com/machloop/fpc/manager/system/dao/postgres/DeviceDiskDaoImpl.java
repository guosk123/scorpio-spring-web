package com.machloop.fpc.manager.system.dao.postgres;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.system.dao.DeviceDiskDao;
import com.machloop.fpc.manager.system.data.DeviceDiskDO;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
@Repository
public class DeviceDiskDaoImpl implements DeviceDiskDao {

  private static final String TABLE_SYSTEM_DEVICE_DISK = "fpc_system_device_disk";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceDiskDao#queryDeviceDisks()
   */
  @Override
  public List<DeviceDiskDO> queryDeviceDisks() {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select id, device_id, physical_location, slot_no, raid_no, raid_level, state, medium, capacity, rebuild_progress, copyback_progress, description ");
    sql.append(" from ").append(TABLE_SYSTEM_DEVICE_DISK);
    sql.append(" order by device_id, slot_no ASC ");

    return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(DeviceDiskDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceDiskDao#saveOrUpdateDeviceDisk(com.machloop.fpc.manager.system.data.DeviceDiskDO)
   */
  @Override
  public int saveOrUpdateDeviceDisk(DeviceDiskDO deviceDisk) {
    if (deviceDisk.getDescription() == null) {
      deviceDisk.setDescription("");
    }
    int update = updateDeviceDisk(deviceDisk);
    return update > 0 ? update : saveDeviceDisk(deviceDisk);
  }

  private int saveDeviceDisk(DeviceDiskDO deviceDisk) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_SYSTEM_DEVICE_DISK);
    sql.append(
        " (id, device_id, physical_location, slot_no, raid_no, raid_level, state, medium, capacity, rebuild_progress, copyback_progress, foreign_state, description ) ");
    sql.append(
        " values (:id, :deviceId, :physicalLocation, :slotNo, :raidNo, :raidLevel, :state, :medium, :capacity, :rebuildProgress, :copybackProgress, :foreignState, :description ) ");

    deviceDisk.setId(IdGenerator.generateUUID());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(deviceDisk);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateDeviceDisk(DeviceDiskDO deviceDisk) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_SYSTEM_DEVICE_DISK);
    sql.append(
        " set raid_no = :raidNo, raid_level = :raidLevel, state = :state, medium = :medium, ");
    sql.append(
        " capacity = :capacity, rebuild_progress = :rebuildProgress, copyback_progress = :copybackProgress, foreign_state = :foreignState, description = :description ");
    sql.append(" where device_id = :deviceId and slot_no = :slotNo ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(deviceDisk);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceDiskDao#deleteDeviceDisk(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteDeviceDisk(String deviceId, String slotNo) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_SYSTEM_DEVICE_DISK);
    sql.append(" where device_id = :deviceId and slot_no = :slotNo ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    paramMap.put("deviceId", deviceId);
    paramMap.put("slotNo", slotNo);

    return jdbcTemplate.update(sql.toString(), paramMap);
  }

}
