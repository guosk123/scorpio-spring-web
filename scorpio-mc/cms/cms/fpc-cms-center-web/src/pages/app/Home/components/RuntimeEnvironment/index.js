import { ChartCard } from '@/components/Charts';
import { SyncOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { connect } from 'dva';
import React, { PureComponent } from 'react';
import runtimeIcon from '../../assets/runtime.svg';
import { customSpin } from '../../utils';
import styles from './index.less';

/**
 * 流量存储统计
 */
@connect(({ appModel: { systemUptimeText, systemTime }, loading }) => ({
  systemUptimeText,
  systemTime,
  queryRuntimeLoading: loading.effects['appModel/queryRuntimeEnvironments'],
}))
class RuntimeEnvironment extends PureComponent {
  componentDidMount() {
    this.queryRuntimeEnvironments();
  }

  queryRuntimeEnvironments = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'appModel/queryRuntimeEnvironments',
    });
  };

  render() {
    // 统计
    const { systemTime, systemUptimeText, chartProps, queryRuntimeLoading } = this.props;

    return (
      <ChartCard
        {...chartProps}
        title="运行时间"
        avatar={<img alt="indicator" style={{ width: 56, height: 56 }} src={runtimeIcon} />}
        total={() =>
          queryRuntimeLoading ? (
            customSpin
          ) : (
            <span className={styles.total}>{systemUptimeText}</span>
          )
        }
        footer={
          <div style={{ height: 24 }}>
            服务器时间：{queryRuntimeLoading ? customSpin : <b>{systemTime}</b>}
          </div>
        }
        action={
          <Tooltip title="点击刷新" onClick={this.queryRuntimeEnvironments}>
            <SyncOutlined style={{ fontSize: 16 }} />
          </Tooltip>
        }
      />
    );
  }
}

export default RuntimeEnvironment;
