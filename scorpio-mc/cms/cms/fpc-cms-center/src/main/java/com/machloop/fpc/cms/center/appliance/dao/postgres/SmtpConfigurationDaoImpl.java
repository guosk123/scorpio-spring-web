package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.SmtpConfigurationDao;
import com.machloop.fpc.cms.center.appliance.data.SmtpConfigurationDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-cms-center
 */
@Repository
public class SmtpConfigurationDaoImpl implements SmtpConfigurationDao {

  public static final String TABLE_APPLIANCE_SMTP_CONFIGURATION = "fpccms_appliance_smtp_configuration";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.SmtpConfigurationDao#querySmtpConfigurations()
   */
  @Override
  public SmtpConfigurationDO querySmtpConfigurations() {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, mail_username, mail_address, smtp_server, server_port, ");
    sql.append(" encrypt, login_user, login_password ");
    sql.append(" from ").append(TABLE_APPLIANCE_SMTP_CONFIGURATION);

    List<SmtpConfigurationDO> smtpConfigurationList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(SmtpConfigurationDO.class));

    return CollectionUtils.isEmpty(smtpConfigurationList) ? new SmtpConfigurationDO()
        : smtpConfigurationList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.SmtpConfigurationDao#saveOrUpdateSmtpConfiguration(com.machloop.fpc.cms.center.appliance.data.SmtpConfigurationDO)
   */
  @Override
  public int saveOrUpdateSmtpConfiguration(SmtpConfigurationDO smtpConfigurationDO) {
   
    int update = updateSmtpConfiguration(smtpConfigurationDO);
    return update > 0 ? update : saveSmtpConfiguration(smtpConfigurationDO);
  }

  private int saveSmtpConfiguration(SmtpConfigurationDO smtpConfigurationDO) {

    StringBuilder sql = new StringBuilder();
    sql.append(" insert into ").append(TABLE_APPLIANCE_SMTP_CONFIGURATION);
    sql.append(" (id, mail_username, mail_address, smtp_server, server_port, encrypt, ");
    sql.append(" login_user, login_password, deleted, update_time, create_time) ");
    sql.append(
        " values (:id, :mailUsername, :mailAddress, :smtpServer, :serverPort, :encrypt, ");
    sql.append(" :loginUser, :loginPassword, :deleted, :updateTime, :createTime) ");

    smtpConfigurationDO.setId(IdGenerator.generateUUID());
    smtpConfigurationDO.setDeleted(Constants.BOOL_NO);
    smtpConfigurationDO.setCreateTime(DateUtils.now());
    smtpConfigurationDO.setUpdateTime(smtpConfigurationDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(smtpConfigurationDO);
    return jdbcTemplate.update(sql.toString(), paramSource);

  }

  private int updateSmtpConfiguration(SmtpConfigurationDO smtpConfigurationDO) {
    
    StringBuilder sql = new StringBuilder();
    sql.append(" update ").append(TABLE_APPLIANCE_SMTP_CONFIGURATION);
    sql.append(" set mail_username = :mailUsername, mail_address = :mailAddress, smtp_server = :smtpServer, ");
    sql.append(" server_port = :serverPort, encrypt = :encrypt, login_user = :loginUser, ");
    sql.append(" login_password = :loginPassword, update_time = :updateTime, operator_id = :operatorId ");
    
    smtpConfigurationDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(smtpConfigurationDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

}
