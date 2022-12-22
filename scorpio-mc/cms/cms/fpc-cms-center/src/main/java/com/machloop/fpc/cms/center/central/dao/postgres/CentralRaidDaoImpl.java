package com.machloop.fpc.cms.center.central.dao.postgres;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.central.dao.CentralRaidDao;
import com.machloop.fpc.cms.center.central.data.CentralRaidDO;

@Repository
public class CentralRaidDaoImpl implements CentralRaidDao {

  private static final String TABLE_DEVICE_RAID = "fpccms_central_device_raid";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralRaidDao#queryRaidBySerialNumber(java.lang.String, java.lang.String)
   */
  @Override
  public List<CentralRaidDO> queryRaidBySerialNumber(String deviceType, String serialNumber) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", serialNumber);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralRaidDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralRaidDao#batchSaveOrUpdateCentralRaids(java.util.List)
   */
  @Override
  public void batchSaveOrUpdateCentralRaids(List<CentralRaidDO> raidList) {
    // delete all raid msg
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_DEVICE_RAID);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", raidList.get(0).getDeviceType());
    params.put("deviceSerialNumber", raidList.get(0).getDeviceSerialNumber());

    jdbcTemplate.update(sql.toString(), params);

    // batch insert raid msg
    sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_DEVICE_RAID);
    sql.append(" (id, device_type, device_serial_number, raid_no, raid_level, state) ");
    sql.append(" values (:id, :deviceType, :deviceSerialNumber, :raidNo, :raidLevel, :state)");

    for (CentralRaidDO fpcRaidDO : raidList) {
      fpcRaidDO.setId(IdGenerator.generateUUID());
    }
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(raidList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralRaidDao#deleteCentralRaids(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralRaids(String deviceType, String serialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_DEVICE_RAID);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", serialNumber);
    
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, device_serial_number, raid_no, raid_level, state from ");
    sql.append(TABLE_DEVICE_RAID);
    return sql;
  }

}
