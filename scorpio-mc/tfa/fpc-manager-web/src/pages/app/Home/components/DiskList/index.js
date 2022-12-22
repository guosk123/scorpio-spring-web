import {
  DEVICE_DISK_STATUS_COPY_BACK,
  DEVICE_DISK_STATUS_FAILED,
  DEVICE_DISK_STATUS_REBUILD,
  DEVICE_NETIF_STATE_UP,
  ONE_KILO_1000,
} from '@/common/dict';
import { Field } from '@/components/Charts';
import { getDiskStateInfo } from '@/utils/utils';
import { ExclamationCircleFilled } from '@ant-design/icons';
import { Card, Col, Divider, Empty, Popover, Row } from 'antd';
import { connect } from 'dva';
import React, { Fragment, PureComponent } from 'react';
import portDownIcon from '../../assets/port_status_down.png';
import portUpIcon from '../../assets/port_status_up.png';
import styles from './index.less';

/**
 * 硬盘和设备接口状态
 */
@connect((state) => {
  const {
    deviceDiskModel: { list: diskList },
    deviceNetifModel: { list: netifList },
    loading: { effects },
  } = state;

  return {
    diskList,
    netifList,
    queryLoading: effects['deviceDiskModel/queryDeviceDisks'],
  };
})
class DiskBox extends PureComponent {
  componentDidMount() {
    this.queryPolling();
    this.timer = setInterval(() => this.queryPolling(), 5000);
  }

  // 消除定时
  componentWillUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  queryPolling = () => {
    const { dispatch } = this.props;
    // raid
    dispatch({ type: 'deviceDiskModel/queryDeviceDisks' });
    // 接口
    dispatch({ type: 'deviceNetifModel/queryDeviceNetifs' });
  };

  render() {
    const { diskList, netifList } = this.props;

    if (diskList.length === 0) {
      return null;
    }

    return (
      <Card title="设备硬盘、设备接口状态" size="small" bordered={false}>
        <div className={styles.wrapper}>
          {diskList.length === 0 && <Empty description="暂无硬盘数据" />}
          <Row className={styles.diskWrap} gutter={10}>
            {diskList.map((item, index) => (
              <Fragment>
                {index > 0 && item.deviceId !== diskList[index - 1].deviceId && <Divider />}
                <Col
                  className={styles.col}
                  span={4}
                  data-slotNo={item.slotNo}
                  data-state={item.state}
                >
                  <div
                    className={styles.diskItem}
                    style={{ borderLeftColor: getDiskStateInfo(item.state).color }}
                  >
                    <p className={styles.title}>
                      {item.physicalLocation}#{item.slotNo}
                    </p>
                    <p>
                      状态：{item.stateText}
                      {item.state === DEVICE_DISK_STATUS_REBUILD &&
                        ` [${item.rebuildProgressText}]`}
                      {item.state === DEVICE_DISK_STATUS_COPY_BACK &&
                        ` [${item.copybackProgressText}]`}
                    </p>
                    <p>介质：{item.mediumText}</p>

                    {/* 硬盘状态为失败或者是有描述信息，就显示提示信息 */}
                    {(item.state === DEVICE_DISK_STATUS_FAILED || item.description) && (
                      <Popover
                        trigger="hover"
                        content={
                          <div>
                            <p style={{ marginBottom: 0, color: '#faad14', maxWidth: 180 }}>
                              <ExclamationCircleFilled />
                              &nbsp; {item.description || '请及时更换磁盘'}
                            </p>
                          </div>
                        }
                      >
                        <ExclamationCircleFilled
                          className={styles.descIcon}
                          style={{ color: '#faad14' }}
                        />
                      </Popover>
                    )}
                  </div>
                </Col>
              </Fragment>
            ))}
          </Row>
          <ul className={styles.portWrap}>
            <li className={styles.portBox}>
              {netifList.map((item) => (
                <Popover
                  content={
                    <div>
                      <Field label="名称" value={item.name} />
                      <Field
                        label="规格"
                        value={
                          item.specification ? `${item.specification / ONE_KILO_1000}Gbps` : ''
                        }
                      />
                      <Field label="用途" value={item.categoryText} />
                      <Field
                        label="状态"
                        value={item.state === DEVICE_NETIF_STATE_UP ? 'up' : 'down'}
                      />
                    </div>
                  }
                  key={item.id}
                >
                  <div
                    className={[styles.portItem].join(' ')}
                    style={{
                      backgroundImage: `url(${
                        item.state === DEVICE_NETIF_STATE_UP ? portUpIcon : portDownIcon
                      })`,
                    }}
                  />
                </Popover>
              ))}
            </li>
          </ul>
        </div>
      </Card>
    );
  }
}

export default DiskBox;
