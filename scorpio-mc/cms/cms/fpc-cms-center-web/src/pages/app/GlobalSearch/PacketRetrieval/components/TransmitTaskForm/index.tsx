import type { ITaskFilter } from '@/pages/app/appliance/Packet/PacketPage';
import { querySensorList } from '@/pages/app/Configuration/equipment/service';
import { bpfValid } from '@/utils/app/utils';
import { parseArrayJson } from '@/utils/utils';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Modal,
  notification,
  Radio,
  Select,
  Space,
  TreeSelect,
} from 'antd';
import { connect } from 'dva';
import lodash from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import React, { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import { createTransmitTask, updateTransmitTask } from '../../service';
import type { IFilterTuple, ITransmitTask } from '../../typings';
import { EReplayForwardAction, EReplayRateUnit } from '../../typings';
import {
  EFilterConditionType,
  ETransmitMode,
  FILTER_CONDITION_TYPE_MAP,
  TASK_MODE_MAP,
} from '../../typings';
import FilterRaw from '../FilterRaw';
import FilterTuple from '../FilterTuple';
import styles from './index.less';

const { TextArea } = Input;

// 默认转发策略
export const FLOW_REPLAY_FORWARD_ACTION_DEFAULT = '1';

/** 过滤条件类型 */
export const FILTER_CONDITION_TYPE_LIST = Object.keys(FILTER_CONDITION_TYPE_MAP).map((key) => ({
  value: key,
  label: FILTER_CONDITION_TYPE_MAP[key],
}));

export const ALL_NETWORK_KEY = 'ALL';

/** 流量导出模式 */
export const TRANSMIT_MODE_LIST = [ETransmitMode.PCAP, ETransmitMode.PCAPNG].map((key) => ({
  value: key,
  label: TASK_MODE_MAP[key],
}));

export const formLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19 },
};

export const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19, offset: 3 },
};

export enum EPageMode {
  /** 新建任务 */
  'Create',
  /** 编辑任务 */
  'Update',
  /** 复制任务 */
  'Copy',
}

export interface IPacketTaskDetail {
  start: string;
  end: string;
  networkId: string;
  taskName: string;
  fpcSerialNumber: string[];
  taskFilter: ITaskFilter;
}
export interface ITransmitTaskFormSharedProps {
  /** 任务详情 */
  detail?: ITransmitTask;
  /** 从数据包新建任务 */
  packetTaskDetail?: IPacketTaskDetail;
  onSubmit?: () => void | undefined;
  showCancelBtn?: boolean;
}

interface ITransmitTaskFormProps extends ITransmitTaskFormSharedProps {
  dispatch: Dispatch;
}
const TransmitTaskForm: React.FC<ITransmitTaskFormProps> = (props) => {
  const { detail, packetTaskDetail, onSubmit, showCancelBtn = true } = props;
  const [form] = Form.useForm();

  useEffect(() => {
    if (packetTaskDetail) {
      const {
        taskName,
        start,
        end,
        networkId,
        fpcSerialNumber: sensorId,
        taskFilter,
      } = packetTaskDetail;
      form.setFieldsValue({
        ...(taskName ? { name: taskName } : {}),
        ...(start ? { filterStartTime: moment(parseInt(start, 10)) } : {}),
        ...(end ? { filterEndTime: moment(parseInt(end, 10)) } : {}),
        ...(networkId ? { filterNetworkId: networkId } : {}),
        ...(sensorId ? { fpcSerialNumber: sensorId } : {}),
        // ...(taskFilter.conditionType ? { filterConditionType: taskFilter.conditionType } : {}),
        ...(taskFilter?.bpf ? { filterBpf: taskFilter.bpf } : {}),
        ...(taskFilter?.rules ? { filterTuple: taskFilter.rules } : {}),
      });
    }
  }, [form, packetTaskDetail]);

  const formDetail = useMemo(() => {
    if (!detail) {
      return {
        // filterConditionType: EFilterConditionType.TUPLE,
        forwardAction: EReplayForwardAction.NO_STORE_REPLAY,
        replayRateUnit: EReplayRateUnit.KBPS,
      };
    }
    return {
      ...detail,
      fpcSerialNumber: detail.fpcSerialNumber ? detail.fpcSerialNumber.split(',') : [],
      // 这里不需要判断类型了，因为这个提交表格里面已经删除这个单选按钮了
      // filterConditionType: detail.filterConditionType || EFilterConditionType.TUPLE,
      filterStartTime: moment(detail?.filterStartTime),
      filterEndTime: moment(detail?.filterEndTime),
    };
  }, [detail]);

  const [sensorList, setSensorList] = useState([]);

  useEffect(() => {
    querySensorList().then((res) => {
      const { success, result } = res;
      if (success) {
        setSensorList(result);
      }
    });
  }, []);

  /** 校验开始时间 */
  const checkFilterStartTime = async (_: any, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请选择过滤条件开始时间'));
    }
    // 截止时间
    const filterEndTime = form.getFieldValue('filterEndTime');
    if (filterEndTime && moment(value).isAfter(moment(filterEndTime), 'second')) {
      return Promise.reject(new Error('开始时间不能晚于截止时间'));
    }
    return Promise.resolve();
  };

  /** 校验截止时间 */
  const checkFilterEndTime = async (_: any, value: string) => {
    if (!value) {
      return Promise.reject(new Error('请选择过滤条件截止时间'));
    }
    // 开始时间
    const filterStartTime = form.getFieldValue('filterStartTime');
    if (filterStartTime && moment(value).isBefore(moment(filterStartTime), 'second')) {
      return Promise.reject(new Error('截止时间不能早于开始时间'));
    }
    return Promise.resolve();
  };

  /**
   * 过滤开始时间发生变化
   */
  const handleStartTimeChange = (time: Moment | null) => {
    const filterEndTime = form.getFieldValue('filterEndTime');
    // 重新触发截止时间的校验
    if (time && filterEndTime) {
      form.validateFields(['filterEndTime']);
    }
  };
  /** 过滤开始时间发生变化 */
  const handleEndTimeChange = (time: Moment | null) => {
    const filterStartTime = form.getFieldValue('filterStartTime');
    // 重新触发开始时间的校验
    if (time && filterStartTime) {
      form.validateFields(['filterStartTime']);
    }
  };
  // ------------过滤时间相关的校验 E--------------

  /** 校验BPF语法是否合法 */
  const debouncedBpfValid = lodash.debounce(bpfValid, 500);

  const goListPage = () => {
    history.goBack();
  };

  const doSubmit = (fields: Record<string, any>) => {
    if (fields.id) {
      updateTransmitTask(fields).then((res) => {
        const { success } = res;
        if (success) {
          goListPage();
        }
      });
    } else {
      createTransmitTask(fields).then((res) => {
        const { success } = res;
        if (success && onSubmit) {
          onSubmit();
        } else if (success) {
          goListPage();
        }
      });
    }
  };

  const handleFinish = (fieldsValue: Record<string, any>) => {
    let data = { ...fieldsValue };
    // 删除临时的字段
    // 因为字段都是驼峰，所以可以其他的都是临时字段
    Object.keys(data).forEach((key) => {
      if (key.indexOf('_') > -1 || key.indexOf('-') > -1) {
        delete data[key];
      }
    });

    data = {
      ...data,
      // 起止时间处理成时间戳
      filterStartTime: moment(data.filterStartTime).format(),
      filterEndTime: moment(data.filterEndTime).format(),
      filterConditionType: EFilterConditionType.ALL,
      filterTuple: data.filterTuple || '[]',
      filterRaw: data.filterRaw || '[]',
      description: data.description || '',
    };

    // 如果是规则条件，组装 json 字段
    // if (data.filterConditionType === EFilterConditionType.TUPLE) {
    //   // bpf 语法设置为空
    //   data.filterBpf = '';
    //   //现在不需要置空了

    const filterTupleList: IFilterTuple[] = parseArrayJson(data.filterTuple);
    const newFilterTupleList = filterTupleList.filter(
      (item) => item && Object.keys(item).length > 0,
    );
    // 比较则组，是否完全不同
    const uniqResult = lodash.uniqWith(newFilterTupleList, lodash.isEqual);
    if (uniqResult.length !== newFilterTupleList.length) {
      notification.warning({
        message: '无法保存',
        description: '不允许存在重复的规则组。请修改后再次保存。',
      });
      return;
    }

    // 如果没有有效的规则组，直接将 json 字段设置为空
    if (newFilterTupleList.length === 0) {
      data.filterTuple = '[]';
    } else {
      data.filterTuple = JSON.stringify(newFilterTupleList);
    }
    // }

    // BPF 过滤时，六元组信息设置为空
    // if (data.filterConditionType === EFilterConditionType.BPF) {
    //   // 现在过滤的时候，是两个条件都有，所以不用再将其中一个置为空
    //   data.filterTuple = '';
    // }

    //现在由前端判断类型
    // console.log(typeof data.filterTuple, 'data.filterTuple');
    // console.log(data.filterBpf, 'data.filterBpf');
    if (data.filterTuple !== '[]' && !data.filterBpf) {
      data.filterConditionType = EFilterConditionType.TUPLE;
    }
    if (data.filterTuple === '[]' && data.filterBpf) {
      data.filterConditionType = EFilterConditionType.BPF;
    }

    // 处理选择全部探针
    const sensorIdsArr: any = [];
    if (fieldsValue.fpcSerialNumber.length === 1 && fieldsValue.fpcSerialNumber[0] === 'ALL') {
      sensorList.forEach((item: any) => {
        console.log('item.serialNumber', item.serialNumber);
        sensorIdsArr.push(item.serialNumber);
      });
      fieldsValue.fpcSerialNumber = sensorIdsArr;
    }

    Modal.confirm({
      title: '确定保存吗？',
      onOk: () => {
        doSubmit({ ...data, fpcSerialNumber: fieldsValue.fpcSerialNumber.join(',') });
      },
    });
  };

  return (
    <Card bordered={false}>
      <Form<ITransmitTask>
        form={form}
        {...formLayout}
        onFinish={handleFinish}
        scrollToFirstError
        initialValues={formDetail}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Form.Item
          name="name"
          label="任务名称"
          rules={[
            { required: true, whitespace: true, message: '请输入任务名称' },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="请输入任务名称" />
        </Form.Item>

        <Form.Item label="过滤条件" style={{ marginBottom: 0 }}>
          <Form.Item
            name="filterStartTime"
            // label="开始时间"
            extra={`选择开始时间`}
            validateFirst
            rules={[
              { required: true, message: '请选择过滤条件开始时间' },
              {
                validator: checkFilterStartTime,
              },
            ]}
          >
            <DatePicker
              dropdownClassName={styles.startTimePicker}
              showTime
              showToday={false}
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择过滤条件开始时间"
              onChange={handleStartTimeChange}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item
            name="filterEndTime"
            // label="截止时间"
            extra="选择截止时间"
            validateFirst
            rules={[
              { required: true, message: '请选择过滤条件截止时间' },
              {
                validator: checkFilterEndTime,
              },
            ]}
          >
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择过滤条件截止时间"
              onChange={handleEndTimeChange}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item
            name="fpcSerialNumber"
            extra="选择探针"
            rules={[{ required: true, message: '请选择探针' }]}
          >
            <TreeSelect
              showCheckedStrategy="SHOW_PARENT"
              treeCheckable={true}
              treeDefaultExpandAll
              treeData={[
                {
                  title: '全部探针',
                  value: 'ALL',
                  key: 'ALL',
                  children: sensorList.map((item: any) => {
                    return { title: item.name, value: item.serialNumber, key: item.serialNumber };
                  }),
                },
              ]}
            />
          </Form.Item>
          {/* 选择 BPF 语法或者是规则条件 */}
          {/* <Form.Item name="filterConditionType" style={{ marginBottom: 0 }}>
            <Radio.Group>
              {FILTER_CONDITION_TYPE_LIST.map((item) => (
                <Radio key={item.value} value={item.value}>
                  {item.label}
                </Radio>
              ))}
            </Radio.Group>
          </Form.Item> */}

          {/* <Form.Item noStyle shouldUpdate>
            {({ getFieldValue }) => {
              const conditionType = getFieldValue('filterConditionType');
              if (conditionType === EFilterConditionType.BPF) {
                return (
                  <Form.Item
                    name="filterBpf"
                    extra="支持标准的BPF语法"
                    rules={[
                      {
                        validator: debouncedBpfValid,
                      },
                    ]}
                  >
                    <Input.TextArea rows={4} placeholder="[选填]请输入BPF语句" />
                  </Form.Item>
                );
              }

              return (
                <Form.Item name="filterTuple">
                  <FilterTuple form={form} ruleProps={packetTaskDetail?.taskFilter.rules} />
                </Form.Item>
              );
            }}
          </Form.Item> */}
          <Form.Item
            name="filterBpf"
            extra="支持标准的BPF语法"
            rules={[
              {
                validator: debouncedBpfValid,
              },
            ]}
          >
            <Input.TextArea rows={4} placeholder="[选填]请输入BPF语句" />
          </Form.Item>
          <Form.Item name="filterTuple">
            <FilterTuple form={form} ruleProps={packetTaskDetail?.taskFilter.rules} />
          </Form.Item>
        </Form.Item>
        {/* 内容匹配 */}
        <Form.Item name="filterRaw" label="内容匹配">
          <FilterRaw form={form} />
        </Form.Item>

        <Form.Item
          name="mode"
          label="导出模式"
          rules={[{ required: true, message: '请选择导出模式' }]}
        >
          <Select style={{ width: '100%' }} placeholder="请选择导出模式">
            {TRANSMIT_MODE_LIST.map((item) => (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="description"
          label="描述信息"
          rules={[{ max: 255, message: '最多可输入255个字符' }]}
        >
          <TextArea rows={4} placeholder="请输入描述信息" />
        </Form.Item>
        <Form.Item {...formTailLayout}>
          <Space>
            <Button type="primary" htmlType="submit">
              保存
            </Button>
            <Button style={showCancelBtn ? {} : { display: 'none' }} onClick={goListPage}>
              返回
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default connect()(TransmitTaskForm);
