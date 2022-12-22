import {
  DEVICE_DISK_STATUS_COPY_BACK,
  DEVICE_DISK_STATUS_FAILED,
  DEVICE_DISK_STATUS_REBUILD,
} from '@/common/dict';
import { getDiskStateInfo } from '@/utils/utils';
import { ExclamationCircleFilled } from '@ant-design/icons';
import { Table, Tag, Tooltip } from 'antd';
import { connect } from 'dva';
import React, { Fragment, PureComponent } from 'react';

@connect((state) => {
  const { deviceDiskModel, loading } = state;
  const { effects } = loading;
  return {
    deviceDiskModel,
    list: deviceDiskModel.list,
    queryLoading: effects['deviceDiskModel/deviceDiskModel'],
  };
})
class DeviceDisk extends PureComponent {
  componentDidMount() {
    this.handleInitData();
    // 定时刷新 RAID 状态
    this.timer = setInterval(() => this.handleInitData(), 3000);
  }

  // 消除定时
  componentWillUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  handleInitData = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'deviceDiskModel/queryDeviceDisks',
    });
  };

  render() {
    const { list } = this.props;
    const thWidth = `${100 / 8}%`;
    const columns = [
      {
        title: '槽位编号',
        dataIndex: 'slotNo',
        width: thWidth,
        align: 'center',
      },
      {
        title: '物理位置',
        dataIndex: 'physicalLocation',
        width: thWidth,
        align: 'center',
      },
      {
        title: '所属RAID组',
        dataIndex: 'raidNo',
        width: thWidth,
        align: 'center',
        render: (text) => text || '--',
      },
      {
        title: 'RAID组级别',
        dataIndex: 'raidLevel',
        width: thWidth,
        align: 'center',
        render: (text) => text || '--',
      },
      {
        title: '状态',
        dataIndex: 'stateText',
        align: 'center',
        width: thWidth,
        render: (stateText, record) => {
          const { state, description } = record;
          const stateInfo = getDiskStateInfo(state);
          if (
            // 失败，错误
            state === DEVICE_DISK_STATUS_FAILED ||
            description
          ) {
            return (
              <Tooltip
                title={
                  <p style={{ marginBottom: 0, color: '#faad14' }}>
                    <ExclamationCircleFilled />
                    &nbsp; {description || '请及时更换磁盘'}
                  </p>
                }
              >
                <Tag style={{ cursor: 'default', marginRight: 0 }} color={stateInfo.status_color}>
                  {stateText}
                </Tag>
              </Tooltip>
            );
          }

          return (
            <Tag style={{ cursor: 'default', marginRight: 0 }} color={stateInfo.status_color}>
              {stateText}
            </Tag>
          );
        },
      },
      {
        title: '存储介质',
        dataIndex: 'mediumText',
        align: 'center',
        width: thWidth,
      },
      {
        title: '大小',
        dataIndex: 'capacityText',
        align: 'center',
        width: thWidth,
      },
      {
        title: '重建/回拷进度',
        dataIndex: 'progressText',
        align: 'center',
        render: (text, record) => {
          const { state, rebuildProgressText, copybackProgressText } = record;
          if (state === DEVICE_DISK_STATUS_REBUILD) {
            return rebuildProgressText;
          }
          if (state === DEVICE_DISK_STATUS_COPY_BACK) {
            return copybackProgressText;
          }

          return 'N/A';
        },
      },
    ];
    return (
      <Fragment>
        <Table
          rowKey={(record) => `${record.slotNo}_${record.deviceId}`}
          columns={columns}
          bordered
          dataSource={list}
          size="middle"
          pagination={false}
        />
      </Fragment>
    );
  }
}

export default DeviceDisk;
