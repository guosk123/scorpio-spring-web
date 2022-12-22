import { ONE_KILO_1024 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import { bytesToSize, getMetricsValue } from '@/utils/utils';
import { Button, Card, Divider, Form, InputNumber, Modal, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import type { Dispatch, IMonitorMetric } from 'umi';
import type { IStorageSpace } from './typings';
import { SpaceEnum } from './typings';

const FormItem = Form.Item;

/**
 * 存储介质：GB 到 Bytes 的换算
 */
const giga2Bytes = ONE_KILO_1024 ** 3;
/**
 * 文件大小：GB 到 Bytes 的换算
 */
const fileGiga2Bytes = ONE_KILO_1024 ** 3;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 10 },
  },
};

interface IStorageSpaceProps {
  dispatch: Dispatch;
  settings: IStorageSpace[];
  metrics: IMonitorMetric[];
  queryLoading: boolean | undefined;
  updateLoading: boolean | undefined;
}
const StorageSpace: React.FC<IStorageSpaceProps> = ({
  dispatch,
  settings,
  metrics,
  queryLoading,
  updateLoading,
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (dispatch) {
      // 先获取统计信息
      dispatch({
        type: 'moitorModel/queryMetrics',
      });

      dispatch({
        type: 'storageSpaceModel/queryStorageSpaceSettings',
      });
    }
  }, [dispatch]);

  const handleSubmit = ({ space }: { space: Record<SpaceEnum, number> }) => {
    Modal.confirm({
      title: '确定保存吗?',
      cancelText: '取消',
      okText: '保存',
      onOk: () => {
        const data: IStorageSpace[] = [];

        Object.keys(space).forEach((el) => {
          const capacity =
            el === SpaceEnum.TRANSMIT_TASK_FILE_LIMIT
              ? space[el] * fileGiga2Bytes
              : space[el] * giga2Bytes;

          data.push({
            spaceType: el as SpaceEnum,
            capacity,
          });
        });

        dispatch({
          type: 'storageSpaceModel/updateStorageSpaceSettings',
          payload: { storageSpaces: JSON.stringify(data) },
        });
      },
      onCancel() {},
    });
  };

  // 系统数据总存储空间
  const storeTotalBytes = +getMetricsValue('fs_store_total_byte', metrics) || 0;
  // 流量存储空间
  const flowDataTotalBytes = +getMetricsValue('fs_data_total_byte', metrics) || 0;

  const initFormValue = {};
  for (let index = 0; index < settings.length; index += 1) {
    const space = settings[index];
    let capacity;
    if (space.capacity) {
      capacity =
        space.spaceType === SpaceEnum.TRANSMIT_TASK_FILE_LIMIT
          ? space.capacity / fileGiga2Bytes
          : space.capacity / giga2Bytes;
    }
    initFormValue[space.spaceType] = capacity;
  }

  return (
    <Card bordered={false}>
      <Skeleton loading={queryLoading} active>
        <Form
          form={form}
          {...formItemLayout}
          scrollToFirstError
          onFinish={handleSubmit}
          initialValues={{ space: initFormValue }}
        >
          <FormItem label="系统数据总存储空间">
            <span className="ant-form-text">{bytesToSize(storeTotalBytes, 3, ONE_KILO_1024)}</span>
          </FormItem>

          <FormItem label="实时流量存储空间">
            {/* 暂时不支持实时流量存储空间配置 */}
            {/* <FormItem
              noStyle
              name={['space', SpaceEnum.FS_DATA]}
              validateFirst
              rules={[
                {
                  required: true,
                  message: '请设置实时流量存储空间',
                },
              ]}
            >
              <InputNumber style={{ width: 200 }} min={1} precision={0} />
            </FormItem> */}
            <span className="ant-form-text">
              {bytesToSize(flowDataTotalBytes, 3, ONE_KILO_1024)}
            </span>
          </FormItem>

          <FormItem label="离线文件存储空间" required>
            <FormItem
              noStyle
              name={['space', SpaceEnum.OFFLINE_PCAP]}
              validateFirst
              rules={[
                {
                  required: true,
                  message: '请设置离线文件存储空间',
                },
              ]}
            >
              <InputNumber style={{ width: 200 }} min={1} precision={0} />
            </FormItem>
            <span className="ant-form-text"> GiB</span>
          </FormItem>

          <FormItem label="查询缓存存储空间" required>
            <FormItem
              noStyle
              name={['space', SpaceEnum.FS_CACHE]}
              dependencies={[['space', SpaceEnum.TRANSMIT_TASK_FILE_LIMIT]]}
              validateFirst
              rules={[
                {
                  required: true,
                  message: '请设置查询缓存存储空间',
                },
              ]}
            >
              <InputNumber style={{ width: 200 }} min={1} precision={0} />
            </FormItem>
            <span className="ant-form-text"> GiB</span>
          </FormItem>

          <Divider />

          <FormItem label="单文件最大落盘大小" required>
            <FormItem
              noStyle
              name={['space', SpaceEnum.TRANSMIT_TASK_FILE_LIMIT]}
              validateFirst
              dependencies={[['space', SpaceEnum.FS_CACHE]]}
              rules={[
                {
                  required: true,
                  message: '请设置单文件最大落盘大小',
                },
                {
                  validator: async (rule, value) => {
                    const cacheSpace = form.getFieldValue(['space', SpaceEnum.FS_CACHE]) || 0;
                    if (cacheSpace > 0 && value >= cacheSpace) {
                      throw new Error(`单文件大小需小于查询缓存存储空间`);
                    }
                  },
                },
              ]}
            >
              <InputNumber style={{ width: 200 }} min={1} precision={0} />
            </FormItem>
            <span className="ant-form-text"> GiB</span>
          </FormItem>

          <FormItem wrapperCol={{ span: 12, offset: 4 }}>
            <Button type="primary" htmlType="submit" loading={updateLoading}>
              保存
            </Button>
          </FormItem>
        </Form>
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({
    storageSpaceModel: { settings },
    moitorModel: { metrics },
    loading: { effects },
  }: ConnectState) => ({
    settings,
    metrics,
    queryLoading:
      effects['storageSpaceModel/queryStorageSpaceSettings'] || effects['moitorModel/queryMetrics'],
    updateLoading: effects['storageSpaceModel/updateStorageSpaceSettings'],
  }),
)(StorageSpace);
