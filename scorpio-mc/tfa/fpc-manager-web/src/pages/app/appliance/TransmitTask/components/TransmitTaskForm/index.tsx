import { DEVICE_NETIF_CATEGORY_REPLAY, ONE_KILO_1000, ONE_KILO_1024 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import type { INetif } from '@/pages/app/configuration/DeviceNetif/typings';
import { bpfValid, bytesToSize, getLinkUrl, parseArrayJson } from '@/utils/utils';
import {
  Button,
  Card,
  DatePicker,
  Divider,
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
import numeral from 'numeral';
import React, { useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch, IMonitorMetric, IMonitorMetricMap, INetworkTreeData } from 'umi';
import { history, useLocation } from 'umi';
import type { IFilterTuple, ITransmitTask } from '../../typings';
import { EIpChgMode, EMacChgMode, EVlanProcesslMode } from '../../typings';
import type { IPcapFile } from '../../typings';
import {
  EFilterConditionType,
  EIpTunnelMode,
  EReplayForwardAction,
  EReplayRateUnit,
  ETransmitMode,
  ETransmitSource,
  FILTER_CONDITION_TYPE_MAP,
  REPLAY_RATE_UNIT_MAP,
  TASK_MODE_MAP,
} from '../../typings';
import { queryPcapById, queryPcapList } from '@/pages/app/analysis/OfflinePcapAnalysis/service';
import FilterRaw from '../FilterRaw';
import FilterTuple from '../FilterTuple';
import IpTunnel from '../IpTunnel';
import styles from './index.less';
import MacChg from '../MacChg';
import VlanProcess from '../VlanProcess';
import IpChg from '../IPChg';
import ServerDiskInfo from '../ServerDiskInfo';
import TransmitTaskSelect from './components/TransmitTaskSelect/index.';

const { TextArea } = Input;
/** pcap文件分页长度 */
const PCAP_PAGE_SIZE = 10;

export const MAX_GIGA = 10;
// 最大 10Gbps
export const MAX_KBPS = MAX_GIGA * Math.pow(ONE_KILO_1000, 2);
// 最小 128Kbps
export const MIN_KBPS = 128;
// 最大 数据包限制
export const MAX_PPS = 2000000;

// VLANID可以配置的范围为：0-4094 （如果不配置，则与0是相同的，相当于查找没有VLAN标签头的流）
export const VLANID_MIN_NUMBER = 0;
export const VLANID_MAX_NUMBER = 4094;

// 源端口、目的端口只支持输出数字，取值范围：1-65535之间
// 分片或者是snmp，端口可以为0
export const PORT_MIN_NUMBER = 0;
export const PORT_MAX_NUMBER = 65535;

// 默认转发策略
export const FLOW_REPLAY_FORWARD_ACTION_DEFAULT = '1';

/** 过滤条件类型 */
export const FILTER_CONDITION_TYPE_LIST = Object.keys(FILTER_CONDITION_TYPE_MAP).map((key) => ({
  value: key,
  label: FILTER_CONDITION_TYPE_MAP[key],
}));

export const ALL_NETWORK_KEY = 'ALL';

/** 流量导出模式 */
export const TRANSMIT_MODE_LIST = [
  ETransmitMode.PCAP,
  ETransmitMode.PCAPNG,
  ETransmitMode.REPLAY,
  ETransmitMode.EXTERNAL_STORAGE,
  // ETransmitMode.INTERNAL_IDS_ENGINE_DETECTION,
].map((key) => ({
  value: key,
  label: TASK_MODE_MAP[key],
}));

/**
 * 流量导出速率单位
 */
export const REPLAY_RATE_UNIT_LIST = Object.keys(REPLAY_RATE_UNIT_MAP).map((key) => ({
  value: key,
  label: REPLAY_RATE_UNIT_MAP[key],
}));

/** 获取重放模式下的转发策略列表 */
const getReplayActionList = (limitBytes: number) => {
  const limitString = bytesToSize(limitBytes, 3, ONE_KILO_1024);
  return [
    {
      value: EReplayForwardAction.NO_STORE_REPLAY,
      label: '不存储，直接转发',
      description: `查询任务不会占用查询缓存，每次重放都需要重新检索加载流量数据，不受 ${limitString} 大小限制`,
    },
    {
      value: EReplayForwardAction.STORE_REPLAY,
      label: '先存储，再转发',
      description: `查询到的流量文件会存储在查询缓存中，多次回放操作时，不需要重新检索加载流量数据，最大容量上限为 ${limitString}`,
    },
  ];
};

/** 重放速率的描述信息 */
export const renderReplayRateDesc = (replayRateUnit: EReplayRateUnit) => {
  return replayRateUnit === EReplayRateUnit.KBPS
    ? `最小支持 ${MIN_KBPS}Kbps，最大支持 ${MAX_GIGA}Gbps（${numeral(MAX_KBPS).format('0,0')}Kbps）`
    : `最大支持 ${numeral(MAX_PPS).format('0,0')}pps`;
};

export const formLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19 },
};

export const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 19, offset: 3 },
};

/**
 * 获取重放模式下的转发策略信息
 * @param {String} action
 * @param {Number} transmitTaskFileLimitBytes
 */
export function getReplayForwardActionInfo(
  action: EReplayForwardAction,
  transmitTaskFileLimitBytes: number,
) {
  const result = getReplayActionList(transmitTaskFileLimitBytes).find(
    (item) => item.value === action,
  );
  if (result) return result;
  return {
    value: '未知状态',
    label: '未知状态',
    description: '',
  };
}

export enum EPageMode {
  /** 新建任务 */
  'Create',
  /** 编辑任务 */
  'Update',
  /** 复制任务 */
  'Copy',
}

export interface ITransmitTaskFormSharedProps {
  /** 页面类型 */
  pageMode?: EPageMode;
  /** 任务详情 */
  detail?: ITransmitTask;
}

interface ITransmitTaskFormProps extends ITransmitTaskFormSharedProps {
  dispatch: Dispatch;
  moitorMetrics: IMonitorMetric[];
  moitorMetricsMap: IMonitorMetricMap;
  /** 网络接口列表 */
  netifList: INetif[];
  /** 主网和子网组成的网络树 */
  networkTree: INetworkTreeData[];
  /** 存储文件PCAP文件最大限制 */
  transmitTaskFileLimitBytes: number;
  submitLoading: boolean | undefined;
  describeMode?: boolean;
  smallSize?: boolean;
}
const TransmitTaskForm: React.FC<ITransmitTaskFormProps> = ({
  dispatch,
  pageMode = EPageMode.Create,
  detail = {} as any,
  networkTree = [],
  netifList = [],
  transmitTaskFileLimitBytes,
  moitorMetricsMap,
  describeMode = true,
  smallSize = false,
  submitLoading,
}) => {
  const [form] = Form.useForm();

  const location = useLocation() as any as {
    query: {
      rules?: string;
      start?: string;
      end?: string;
      networkId?: string;
      taskName?: string;
      pcapFileId?: string;
      pcapFileName?: string;
      bpfData?: string;
    };
  };

  const { taskName, start, end, networkId } = location.query;
  const replayRule = useMemo(() => {
    if (!(detail as any).replayRule) {
      return {};
    }
    const ruleList = JSON.parse((detail as any).replayRule);
    const result: any = {};
    for (let i = 0; i < ruleList.length; i++) {
      const ruleItem = ruleList[i];
      if (ruleItem.type === 'vlanProcess') {
        result.vlanProcess = JSON.stringify(ruleItem);
      } else if (ruleItem.type === 'macChg') {
        result.macChg = JSON.stringify(ruleItem);
      } else if (ruleItem.type === 'ipChg') {
        result.ipChg = JSON.stringify(ruleItem);
      }
    }
    return result;
  }, [detail]);
  /** 全流量查询来源 */
  const [transmitTaskSource, setTransmitTaskSource] = useState<ETransmitSource>(
    ETransmitSource.NETWORK,
  );

  const [selectedPcapFile, setSelectedPcapFile] = useState<IPcapFile>();

  const [filterConditionTypeRadio, setfilterConditionTypeRadio] = useState<any>(() => {
    let tmpRes = undefined;
    if (detail.filterConditionType) {
      tmpRes = String(detail.filterConditionType);
    }
    tmpRes = location.query.bpfData ? EFilterConditionType.BPF : EFilterConditionType.TUPLE;
    return tmpRes;
  });

  const currentPcapFileId = useMemo(() => {
    return location.query.pcapFileId || detail.filterPacketFileId;
  }, [location.query.pcapFileId, detail.filterPacketFileId]);

  // useEffect(() => {
  //   (async () => {
  //     if (currentPcapFileId) {
  //       const { result, success } = await queryPcapById(currentPcapFileId);
  //       if (success) {
  //         setCurrentPcapFileName(result?.name);
  //       }
  //     }
  //   })();
  // }, [currentPcapFileId]);

  useEffect(() => {
    const { pcapFileId, pcapFileName, bpfData } = location.query;
    const { filterPacketFileId } = detail;
    if (bpfData) {
      form.setFieldsValue({
        // filterConditionType: EFilterConditionType.BPF,
        filterBpf: bpfData || '',
      });
    }
    if (pcapFileName !== undefined && pcapFileId !== undefined) {
      /** 如果有pcapFileId和pcapFileName，就修改来源为pcap文件 */
      (async () => {
        const { result, success } = await queryPcapById(currentPcapFileId);
        if (success) {
          setTransmitTaskSource(ETransmitSource.PCAPFILE);
          if (result?.id) {
            setSelectedPcapFile(result);
            form.setFieldsValue({
              filterPacketFileId: result?.id,
            });
          }
        }
      })();
      form.setFieldsValue({ filterPacketFileId: pcapFileId });
    }
    /** 编辑进入时，初始任务来源 */
    if (filterPacketFileId) {
      setTransmitTaskSource(ETransmitSource.PCAPFILE);
    }
  }, []);

  // useEffect(() => {
  //   if (currentPcapFileName && currentPcapFileId) {
  //     pcapFileList.push({
  //       id: currentPcapFileId,
  //       name: currentPcapFileName,
  //     });
  //   }
  // }, [currentPcapFileName, currentPcapFileId]);

  useEffect(() => {
    setTimeout(() => {
      // fetchPcapFiles('');
    });
  }, []);

  /** 编辑模式下有些字段不允许编辑 */
  const fieldReadonly = useMemo(() => {
    return pageMode === EPageMode.Update;
  }, [pageMode]);

  // -----------导出模式 S---------------
  /** 查询接口 */
  const handleQueryDeviceNetifs = () => {
    dispatch({
      type: 'deviceNetifModel/queryDeviceNetifs',
    });
  };

  /** 查询网络 */
  const handleQueryNetworkTree = () => {
    dispatch({
      type: 'networkModel/queryNetworkTree',
    });
    // 查询系统监控
    dispatch({ type: 'moitorModel/queryMetrics' });
  };

  // -----------过滤时间相关的校验 S---------------
  /** 最早报文时间 */
  const dataOldestTime = useMemo(() => {
    if (selectedPcapFile?.packetStartTime) {
      return selectedPcapFile?.packetStartTime;
    }
    const oldestTime = moitorMetricsMap.data_oldest_time?.metricValue;
    if (!oldestTime) {
      return '';
    }
    return moment(+oldestTime * 1000).format('YYYY-MM-DD HH:mm:ss');
  }, [moitorMetricsMap, selectedPcapFile]);

  useEffect(() => {
    form.setFieldsValue({
      ...(taskName ? { name: taskName } : {}),
      ...(start ? { filterStartTime: moment(parseInt(start, 10)) } : {}),
      ...(end ? { filterEndTime: moment(parseInt(end, 10)) } : {}),
      ...(networkId ? { filterNetworkId: networkId } : {}),
    });

    if (
      dataOldestTime &&
      start &&
      moment(parseInt(start, 10)).isBefore(moment(dataOldestTime), 'second')
    ) {
      form.setFieldsValue({
        ...(start ? { filterStartTime: moment(dataOldestTime) } : {}),
      });
    }
  }, [dataOldestTime, end, form, networkId, taskName, start]);

  useEffect(()=>{
    (async () => {
      const { result, success } = await queryPcapById(currentPcapFileId);
      if (success && result?.id) {
        setSelectedPcapFile(result);
        form.setFieldsValue({
          filterPacketFileId: result?.id,
        });
      }
    })();
  },[currentPcapFileId, form])

  useEffect(() => {
    handleQueryDeviceNetifs();
    handleQueryNetworkTree();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** 禁用时间 */
  const disabledDate = (current: any) => {
    if (current) {
      if (
        dataOldestTime &&
        current.clone().startOf('day') <= moment(dataOldestTime).add(-1, 'day')
      ) {
        return true;
      }
      if (selectedPcapFile) {
        if (current.clone().endOf('day') >= moment(selectedPcapFile?.packetEndTime).add(1, 'day')) {
          return true;
        } else if (current.clone().endOf('day') >= moment().add(1, 'day')) {
          return true;
        }
      }
    }
    return false;
  };

  /** 校验开始时间 */
  const checkFilterStartTime = async (_: any, value: string) => {
    if (selectedPcapFile) {
      return Promise.resolve();
    }
    if (!value) {
      return Promise.reject(new Error('请选择过滤条件开始时间'));
    }
    // 截止时间
    const filterEndTime = form.getFieldValue('filterEndTime');
    if (dataOldestTime && moment(value).isBefore(moment(dataOldestTime), 'second')) {
      return Promise.reject(new Error('开始时间不能早于最早报文时间'));
    }
    if (filterEndTime && moment(value).isAfter(moment(filterEndTime), 'second')) {
      return Promise.reject(new Error('开始时间不能晚于截止时间'));
    }
    return Promise.resolve();
  };

  /** 手动更新开始时间 */
  const setStartTime = () => {
    if (dataOldestTime) {
      form.setFieldsValue({
        filterStartTime: moment(dataOldestTime),
      });
    }
  };

  /** 手动更新结束时间 */
  const setEndTime = () => {
    if (selectedPcapFile?.packetEndTime) {
      form.setFieldsValue({
        filterEndTime: moment(selectedPcapFile?.packetEndTime).add(1, 'second'),
      });
    }
  };

  /** 校验截止时间 */
  const checkFilterEndTime = async (_: any, value: string) => {
    if (selectedPcapFile) {
      return Promise.resolve();
    }
    if (!value) {
      return Promise.reject(new Error('请选择过滤条件截止时间'));
    }
    // 开始时间
    const filterStartTime = form.getFieldValue('filterStartTime');
    // 截止时间
    if (dataOldestTime && moment(value).isBefore(moment(dataOldestTime), 'second')) {
      return Promise.reject(new Error('截止时间不能早于最早报文时间'));
    }
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

  const handleModeChange = (value: ETransmitMode) => {
    // 重放模式
    if (value === ETransmitMode.REPLAY) {
      handleQueryDeviceNetifs();
    }
  };
  // -----------导出模式 E---------------

  /** 校验转发速率 */
  const checkReplayRate = async (rule: any, value: number) => {
    if (!value) {
      return Promise.resolve();
    }
    const replayRateUnit = form.getFieldValue('replayRateUnit');

    // bps, 最小限制 128Kbps，最大限制 10Gbps,
    if (replayRateUnit === EReplayRateUnit.KBPS) {
      if (value < MIN_KBPS) {
        return Promise.reject(`最小支持 ${MIN_KBPS}Kbps`);
      }
      if (value > MAX_KBPS) {
        return Promise.reject(`最大支持 ${MAX_GIGA}Gbps（${numeral(MAX_KBPS).format('0,0')}Kbps）`);
      }
    }
    if (replayRateUnit === EReplayRateUnit.PPS && value > MAX_PPS) {
      // pps, 不能大于 200W
      return Promise.reject(`最大支持${numeral(MAX_PPS).format('0,0')}pps`);
    }
    return Promise.resolve();
  };

  const goListPage = () => {
    history.push(getLinkUrl('/analysis/trace/transmit-task'));
  };

  const doSubmit = (fields: Record<string, any>) => {
    const dispatchType = fields.id ? 'updateTransmitTask' : 'createTransmitTask';
    dispatch({
      type: `transmitTaskModel/${dispatchType}`,
      payload: fields,
    }).then((success: boolean) => {
      if (!describeMode) {
        form.resetFields();
        return;
      }
      if (success) {
        // 编辑时，提示修改成功，不返回上一页
        // if (fields.id) {
        //   this.queryDetail(fields.id);
        //   return;
        // }
        goListPage();
      }
    });
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
      filterNetworkId:
        transmitTaskSource === ETransmitSource.NETWORK ? data.filterNetworkId : undefined,
      filterPacketFileId:
        transmitTaskSource === ETransmitSource.PCAPFILE ? selectedPcapFile?.id : undefined,
      filterPacketFileName:
        transmitTaskSource === ETransmitSource.PCAPFILE ? selectedPcapFile?.name : undefined,
      // 起止时间处理成时间戳
      filterStartTime: moment(data.filterStartTime).format(),
      filterEndTime: moment(data.filterEndTime).format(),
      filterTuple: data.filterTuple || '[]',
      filterRaw: data.filterRaw || '[]',
      description: data.description || '',
    };

    // 如果是规则条件，组装 json 字段
    // if (data.filterConditionType === EFilterConditionType.TUPLE) {
    // bpf 语法设置为空
    // data.filterBpf = '';
    // 现在不需要置空了

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
    // 现在过滤的时候，是两个条件都有，所以不用再将其中一个置为空
    // data.filterTuple = '';
    // }

    // 判断隧道封装
    let ipTunnelJson = data.ipTunnel;
    if (!ipTunnelJson) {
      ipTunnelJson = '';
    } else if (ipTunnelJson.mode === EIpTunnelMode.NONE) {
      ipTunnelJson = '';
    }
    data.ipTunnel = ipTunnelJson ? JSON.stringify(ipTunnelJson) : '';

    const replay_rule = [];
    // 判断vlanid
    let vlanProcessJson = data.vlanProcess;
    if (!vlanProcessJson) {
      vlanProcessJson = '';
    }
    if (vlanProcessJson) {
      const vlanItem = {
        ...vlanProcessJson,
        type: 'vlanProcess',
      };
      if (vlanProcessJson.mode === EVlanProcesslMode.NEWVLAN) {
        delete vlanItem?.rule?.vlanIdAlteration;
        delete vlanItem?.rule?.extraVlanIdRule;
      } else if (vlanProcessJson.mode === EVlanProcesslMode.CHGCLANID) {
        delete vlanItem?.rule?.vlanId;
        delete vlanItem?.rule?.processType;
        if (vlanItem.rule) {
          vlanItem.rule.extraVlanIdRule = vlanItem?.rule?.extraVlanIdRule === true ? '1' : '0';
        }
      } else {
        vlanItem.rule = {};
      }
      replay_rule.push(vlanItem);
    }
    delete data.vlanProcess;

    // 判断mac chg
    let macChgJson = data.macChg;
    if (!macChgJson) {
      macChgJson = '';
    }
    if (macChgJson) {
      const macItem = {
        ...macChgJson,
        type: 'macChg',
      };
      if (macItem.mode === EMacChgMode.NOT_CHANGE) {
        macItem.rule = {};
      }

      replay_rule.push(macItem);
    }
    delete data.macChg;

    // 判断ip chg
    let ipChgJson = data.ipChg;
    if (!ipChgJson) {
      ipChgJson = '';
    }
    if (ipChgJson) {
      const ipItem = {
        ...ipChgJson,
        type: 'ipChg',
      };
      if (ipItem.mode === EIpChgMode.NOT_CHANGE) {
        ipItem.rule = {};
      }

      replay_rule.push(ipItem);
    }
    delete data.ipChg;
    data.replayRule = JSON.stringify(replay_rule);

    Modal.confirm({
      title: '确定保存吗？',
      onOk: () => {
        doSubmit(data);
      },
    });
  };
console.log(selectedPcapFile,'selectedPcapFile')
  return (
    <Card bordered={false}>
      <Form<ITransmitTask>
        form={form}
        {...formLayout}
        onFinish={handleFinish}
        scrollToFirstError
        initialValues={{
          ...detail,
          filterStartTime: detail.filterStartTime ? moment(detail.filterStartTime) : undefined,
          filterEndTime: detail.filterEndTime ? moment(detail.filterEndTime) : undefined,
          // 这里不需要判断类型了，因为这个提交表格里面已经删除这个单选按钮了
          // filterConditionType: detail.filterConditionType || EFilterConditionType.TUPLE,

          ipTunnel: detail.ipTunnel,
          replayRate: detail.replayRate ? +detail.replayRate : undefined,
          replayRateUnit: detail.replayRateUnit || EReplayRateUnit.KBPS,

          forwardAction: detail.forwardAction
            ? detail.forwardAction
            : EReplayForwardAction.NO_STORE_REPLAY,
        }}
      >
        <Form.Item name="id" hidden>
          <Input />
        </Form.Item>
        <Form.Item
          name="name"
          label="名称"
          rules={[
            { required: true, whitespace: true, message: '请输入名称' },
            { max: 30, message: '最多可输入30个字符' },
          ]}
        >
          <Input placeholder="请输入名称" />
        </Form.Item>
        <Form.Item label="过滤网络">
          <Radio.Group
            onChange={(e) => {
              setTransmitTaskSource(e.target.value);
              setSelectedPcapFile(undefined);
              form.setFieldsValue({
                filterPacketFileId: '',
                filterNetworkId: '',
              });
            }}
            value={transmitTaskSource}
            style={{ marginBottom: '10px' }}
            disabled={fieldReadonly}
          >
            <Radio value={ETransmitSource.NETWORK}>网络</Radio>
            <Radio value={ETransmitSource.PCAPFILE}>离线任务</Radio>
          </Radio.Group>
          {transmitTaskSource === ETransmitSource.NETWORK ? (
            <Form.Item
              name="filterNetworkId"
              rules={[
                { required: transmitTaskSource === ETransmitSource.NETWORK, message: '请选择网络' },
              ]}
              extra="选择网络(必填)"
              style={{ width: '100%' }}
            >
              <TreeSelect
                style={{ width: '100%' }}
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={[{ title: '全部网络', value: ALL_NETWORK_KEY }, ...networkTree]}
                placeholder="请选择网络"
                treeDefaultExpandAll
                showSearch
                filterTreeNode={(inputValue, treeNode) => {
                  if (!inputValue) {
                    return true;
                  }
                  return (treeNode!.title as string).indexOf(inputValue) > -1;
                }}
                onSelect={() => {
                  form.setFieldsValue({
                    filterStartTime: '',
                    filterEndTime: '',
                  });
                }}
                disabled={transmitTaskSource !== ETransmitSource.NETWORK || fieldReadonly}
              />
            </Form.Item>
          ) : (
            <Form.Item
              name="filterPacketFileId"
              extra="选择离线文件(必填)"
              rules={[
                {
                  required: transmitTaskSource === ETransmitSource.PCAPFILE,
                  message: '请选择离线文件',
                },
              ]}
            >
              <TransmitTaskSelect<IPcapFile>
                placeholder="请选择离线文件"
                disabled={transmitTaskSource !== ETransmitSource.PCAPFILE || fieldReadonly}
                fetch={queryPcapList}
                initialItem={selectedPcapFile}
                onSelect={(pcapFile: IPcapFile) => {
                  setSelectedPcapFile(pcapFile);
                  form.setFieldsValue({
                    filterPacketFileId: pcapFile?.id,
                    filterStartTime: '',
                    filterEndTime: '',
                  });
                }}
              />
            </Form.Item>
          )}
        </Form.Item>
        <Form.Item label="时间范围" style={{ marginBottom: 0 }}>
          <Form.Item
            name="filterStartTime"
            // label="开始时间"
            extra={`选择开始时间(必填)。${
              selectedPcapFile ? '离线文件启始时间' : '系统存储的最早报文时间'
            }：${dataOldestTime || '没有查询到相关时间'}`}
            validateFirst
            rules={[
              { required: true, message: '请选择过滤条件开始时间' },
              {
                validator: checkFilterStartTime,
              },
            ]}
          >
            <DatePicker
              dropdownClassName={
                selectedPcapFile ? styles.startTimePicker_hide_now : styles.startTimePicker
              }
              showTime
              showToday={false}
              disabled={fieldReadonly}
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择过滤条件开始时间"
              disabledDate={disabledDate}
              onChange={handleStartTimeChange}
              style={{ width: '100%' }}
              renderExtraFooter={() =>
                dataOldestTime && (
                  <a onClick={setStartTime}>{selectedPcapFile ? '离线文件启始时间' : '最早时间'}</a>
                )
              }
            />
          </Form.Item>
          <Form.Item
            name="filterEndTime"
            // label="截止时间"
            extra="选择截止时间(必填)"
            validateFirst
            rules={[
              { required: true, message: '请选择过滤条件截止时间' },
              {
                validator: checkFilterEndTime,
              },
            ]}
          >
            <DatePicker
              dropdownClassName={selectedPcapFile ? styles.endTimePicker_hide_now : ''}
              showTime
              disabled={fieldReadonly}
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择过滤条件截止时间"
              disabledDate={disabledDate}
              onChange={handleEndTimeChange}
              style={{ width: '100%' }}
              {...{
                ...(() => {
                  if (selectedPcapFile) {
                    return {
                      renderExtraFooter: () =>
                        dataOldestTime && <a onClick={setEndTime}>离线文件结束时间</a>,
                    };
                  }
                  return {};
                })(),
              }}
            />
          </Form.Item>
        </Form.Item>
        {/* 选择 BPF 语法或者是规则条件 */}
        {/* <Form.Item label="条件类型" name="filterConditionType" style={{ marginBottom: 0 }}>
          <Radio.Group
            onChange={(event) => {
              setfilterConditionTypeRadio(event.target.value);
            }}
            disabled={fieldReadonly}
          >
            {FILTER_CONDITION_TYPE_LIST.map((item) => (
              <Radio key={item.value} value={item.value}>
                {item.label}
              </Radio>
            ))}
          </Radio.Group>
        </Form.Item> */}

        {/* {
          // const conditionType = getFieldValue('filterConditionType');
          filterConditionTypeRadio === EFilterConditionType.BPF ? (
            <Form.Item
              name="filterBpf"
              extra="支持标准的BPF语法"
              rules={[
                {
                  validator: debouncedBpfValid,
                },
              ]}
              {...formTailLayout}
            >
              <Input.TextArea rows={4} disabled={fieldReadonly} placeholder="[选填]请输入BPF语句" />
            </Form.Item>
          ) : (
            <Form.Item name="filterTuple" {...formTailLayout}>
              <FilterTuple form={form} describeMode={describeMode} smallSize={smallSize} />
            </Form.Item>
          )
        } */}

        <Form.Item label="条件类型" name="filterTuple">
          <FilterTuple form={form} describeMode={describeMode} smallSize={smallSize} />
        </Form.Item>

        <Form.Item
          label="BPF语句"
          name="filterBpf"
          extra="支持标准的BPF语法"
          rules={[
            {
              validator: debouncedBpfValid,
            },
          ]}
          // {...formTailLayout}
        >
          <Input.TextArea rows={4} disabled={fieldReadonly} placeholder="[选填]请输入BPF语句" />
        </Form.Item>

        {/* 内容匹配 */}
        <Form.Item name="filterRaw" label="内容匹配">
          <FilterRaw
            form={form}
            readonly={fieldReadonly}
            describeMode={describeMode}
            smallSize={smallSize}
          />
        </Form.Item>

        <Form.Item
          name="mode"
          label="导出模式"
          rules={[{ required: true, message: '请选择导出模式' }]}
        >
          <Select
            style={{ width: '100%' }}
            placeholder="请选择导出模式"
            disabled={fieldReadonly}
            onChange={handleModeChange}
          >
            {TRANSMIT_MODE_LIST.map((item) => (
              <Select.Option
                disabled={
                  item.value === ETransmitMode.PCAP &&
                  transmitTaskSource === ETransmitSource.PCAPFILE
                }
                key={item.value}
                value={item.value}
              >
                {item.label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item noStyle shouldUpdate>
          {({ getFieldValue }) => {
            const transmitMode = getFieldValue('mode');
            const forwardAction = getFieldValue('forwardAction');
            const replayRateUnit = getFieldValue('replayRateUnit');

            if (transmitMode === ETransmitMode.REPLAY) {
              return (
                <>
                  <Form.Item
                    name="replayNetif"
                    label="转发接口"
                    rules={[{ required: true, message: '请选择转发接口' }]}
                  >
                    <Select style={{ width: '100%' }} placeholder="请选择转发接口">
                      {netifList.map(
                        (item) =>
                          item.category === DEVICE_NETIF_CATEGORY_REPLAY && (
                            <Select.Option key={item.id} value={item.name}>
                              {item.name}
                            </Select.Option>
                          ),
                      )}
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="replayRate"
                    label="转发速率"
                    extra={renderReplayRateDesc(replayRateUnit)}
                    validateFirst
                    rules={[
                      {
                        required: true,
                        message: '请填写转发速率',
                      },
                      {
                        pattern: /^(?!(0[0-9]{0,}$))[0-9]{1,}[.]{0,}[0-9]{0,}$/,
                        message: '只能填写数字且必须大于0',
                      },
                      {
                        validator: checkReplayRate,
                      },
                    ]}
                  >
                    <Input
                      style={{ width: '100%' }}
                      addonAfter={
                        <Form.Item name="replayRateUnit" noStyle>
                          <Select style={{ width: 90 }}>
                            {REPLAY_RATE_UNIT_LIST.map((item) => (
                              <Select.Option key={item.value} value={item.value}>
                                {item.label}
                              </Select.Option>
                            ))}
                          </Select>
                        </Form.Item>
                      }
                      placeholder="请填写转发速率"
                    />
                  </Form.Item>

                  <Form.Item
                    label="转发策略"
                    name="forwardAction"
                    extra={
                      forwardAction
                        ? getReplayForwardActionInfo(forwardAction, transmitTaskFileLimitBytes)
                            .description
                        : ''
                    }
                    rules={[{ required: true, message: '请选择转发策略' }]}
                  >
                    <Select
                      style={{ width: '100%' }}
                      disabled={fieldReadonly}
                      placeholder="请选择转发策略"
                    >
                      {getReplayActionList(transmitTaskFileLimitBytes).map((item) => (
                        <Select.Option key={item.value} value={item.value}>
                          {item.label}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>

                  <Form.Item name="ipChg" noStyle>
                    <IpChg
                      form={form}
                      value={(detail as any).ipChg}
                      init={(replayRule as any).ipChg}
                      pageMode={pageMode}
                      smallSize={smallSize}
                    />
                  </Form.Item>
                  <Form.Item name="ipTunnel" shouldUpdate={false} noStyle>
                    <IpTunnel form={form} value={(detail as any).ipTunnel} smallSize={smallSize} />
                  </Form.Item>
                  <Form.Item name="vlanProcess" noStyle>
                    <VlanProcess
                      form={form}
                      value={(detail as any).VlanProcess}
                      init={(replayRule as any).vlanProcess}
                      smallSize={smallSize}
                    />
                  </Form.Item>
                  <Form.Item name="macChg" noStyle>
                    <MacChg
                      form={form}
                      value={(detail as any).macChg}
                      init={String((replayRule as any).macChg)}
                      smallSize={smallSize}
                    />
                  </Form.Item>
                </>
              );
            }
            return null;
          }}
        </Form.Item>

        <Form.Item
          name="description"
          label="描述信息"
          extra={<ServerDiskInfo />}
          rules={[{ max: 255, message: '最多可输入255个字符' }]}
        >
          <TextArea rows={4} placeholder="请输入描述信息" />
        </Form.Item>
        <Form.Item {...formTailLayout}>
          <Space style={describeMode ? {} : { display: 'flex', justifyContent: 'center' }}>
            {describeMode ? (
              <>
                <Button loading={submitLoading} type="primary" htmlType="submit">
                  保存
                </Button>
                <Button disabled={submitLoading} onClick={goListPage}>
                  返回
                </Button>
              </>
            ) : (
              <Button
                // style={{ width: 240 }}
                loading={submitLoading}
                type="primary"
                htmlType="submit"
              >
                新建
              </Button>
            )}
          </Space>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default connect(
  ({
    loading: { effects },
    deviceNetifModel: { list },
    storageSpaceModel: { transmitTaskFileLimitBytes },
    moitorModel: { metrics, metricsMap },
    networkModel: { networkTree },
  }: ConnectState) => ({
    moitorMetrics: metrics,
    moitorMetricsMap: metricsMap,
    netifList: list,
    networkTree,
    transmitTaskFileLimitBytes,
    submitLoading:
      effects['transmitTaskModel/createTransmitTask'] ||
      effects['transmitTaskModel/updateTransmitTask'],
  }),
)(TransmitTaskForm);
