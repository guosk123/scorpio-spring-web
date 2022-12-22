import { DEVICE_NETIF_CATEGORY_MANAGER, STATS_TIME_RANGE } from '@/common/dict';
import * as dateMath from '@/utils/frame/datemath';
import { ClockCircleOutlined } from '@ant-design/icons';
import { Card, Col, Row, Select } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { Fragment, PureComponent } from 'react';
import { history } from 'umi';
import ManagerNetifStats from './components/ManagerNetifStats';
import ManagerRuntimeEnvironment from './components/ManagerRuntimeEnvironment';
import SystemStateMetricsChart from './components/SystemStateMetricsChart';
import styles from './index.less';

@connect((state) => {
  const {
    moitorModel: { metrics },
    deviceNetifModel: { list },
    loading: { effects },
  } = state;
  return {
    metrics,
    deviceList: list,
    queryRuntimeLoading: effects['globalModel/queryRuntimeEnvironments'],
  };
})
class SystemMonitor extends PureComponent {
  state = {
    from: STATS_TIME_RANGE[0].key,
  };

  static getDerivedStateFromProps(nextProps) {
    const {
      location: { query },
    } = nextProps;
    if (query.from) {
      return {
        from: query.from,
      };
    }
    return null;
  }

  componentDidMount() {
    const { dispatch } = this.props;
    this.queryRuntimeEnvironments();
    // 获取统计
    dispatch({ type: 'moitorModel/queryMetrics' });
    // 获取接口列表
    dispatch({ type: 'deviceNetifModel/queryDeviceNetifs' });
  }

  queryRuntimeEnvironments = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'globalModel/queryRuntimeEnvironments',
    });
  };

  componentWillUnmount() {}

  handleTimeChange = (value) => {
    history.push({
      query: {
        from: value,
        to: 'now',
        t: new Date().getTime(),
      },
    });
  };

  render() {
    const { from } = this.state;

    const { deviceList = [], location } = this.props;

    const fromTime = moment(dateMath.parse(from)).format();
    const toTime = moment().format();
    // 计算时间

    // 管理口网卡
    const managerNetif = deviceList.find((item) => item.category === DEVICE_NETIF_CATEGORY_MANAGER);

    return (
      <Fragment>
        <div className={styles.selectWrapper}>
          <ManagerRuntimeEnvironment />
          <div className={styles.selectBox}>
            <ClockCircleOutlined className={styles.icon} />
            <Select
              showArrow={false}
              style={{ width: '100%' }}
              onChange={this.handleTimeChange}
              value={location.query.from || STATS_TIME_RANGE[0].key}
            >
              {STATS_TIME_RANGE.map((item) => (
                <Select.Option key={item.key} value={item.key}>
                  {item.name}
                </Select.Option>
              ))}
            </Select>
          </div>
        </div>

        {/* {cpuAndMemoryStatsView} */}
        <Card
          size="small"
          title="系统状态"
          bodyStyle={{ padding: '10px 10px 0' }}
          style={{ marginBottom: 10 }}
        >
          <SystemStateMetricsChart from={fromTime} to={toTime} canZoom={false} />
        </Card>
        <Row>
          <Col span={24}>{managerNetif && <ManagerNetifStats data={managerNetif} />}</Col>
        </Row>
      </Fragment>
    );
  }
}

export default SystemMonitor;
