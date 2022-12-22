import type { ConnectState } from '@/models/connect';
import type {
  ICountry,
  ICountryMap,
  IProvince,
  IProvinceMap,
  ICity,
  ICityMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import {
  PORT_MIN_NUMBER,
  PORT_MAX_NUMBER,
  VLANID_MIN_NUMBER,
  VLANID_MAX_NUMBER,
} from '@/pages/app/Configuration/IngestPolicy/components/IngestPolicyForm';
import type {
  ApplicationItem,
  IApplicationMap,
} from '@/pages/app/Configuration/SAKnowledge/typings';
import { ipV4Regex, ipV6Regex } from '@/utils/utils';
import { CloseOutlined, PlusOutlined } from '@ant-design/icons';
import type { FormInstance } from 'antd';
import { Button, Card, Form, Input, Popconfirm, Select, Tag } from 'antd';
import _ from 'lodash';
import React, { useEffect, useState } from 'react';
import { connect, useLocation } from 'umi';
import type { IL7Protocol, IL7ProtocolMap } from '../../../../appliance/Metadata/typings';
import type { IFilterTuple } from '../../typings';
import styles from './index.less';

// 规则条件最大的条数限制
export const TUPLE_MAX_COUNT = 5;

export const filterTupleExtra = (
  <section>
    <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
      <li>源IP、目的IP支持 A.B.C.D 格式的单IP或 A.B.C.D/E 格式的网段</li>
      <li>源IP、目的IP支持IPv6网络</li>
      {/* PORT_MIN_NUMBER, PORT_MAX_NUMBER值在这里为undefined，因此使用静态值替换常量 */}
      {/* <li>
        源端口、目的端口可以配置的范围为：[{PORT_MIN_NUMBER}, {PORT_MAX_NUMBER}]
      </li>
      <li>
        VLANID可以配置的范围为：[{VLANID_MIN_NUMBER}, {VLANID_MAX_NUMBER}
        ]，如果想查找没有VLAN标签头的流请配置为: 0
      </li> */}
      <li>源端口、目的端口可以配置的范围为：[0, 65535]</li>
      <li>VLANID可以配置的范围为：[0, 4094 ]，如果想查找没有VLAN标签头的流请配置为: 0</li>
      <li>配置多组规则时，规则组之间至少需要有1项内容不同，即不允许存在完全相同的两组规则配置</li>
      <li>输入范围时，只能输入类似于1-10这样的范围条件</li>
      <li>一组规则中所有的值都为空时，保存时将会被忽略</li>
      <li>最多可以配置{TUPLE_MAX_COUNT}个有效规则组</li>
      <li>规则组与规则组之间为[或]的关系</li>
      <li>规则组内的条件之间为[与]的关系</li>
    </ul>
  </section>
);

export const FIELD_IP = 'ip';
export const FIELD_SOURCE_IP = 'sourceIp';
export const FIELD_DEST_IP = 'destIp';
export const FIELD_PORT = 'port';
export const FIELD_SOURCE_PORT = 'sourcePort';
export const FIELD_DEST_PORT = 'destPort';
export const FIELD_MAC = 'macAddress';
export const FIELD_SOURCE_MAC = 'sourceMacAddress';
export const FIELD_DEST_MAC = 'destMacAddress';
export const FIELD_VLANID = 'vlanId';
export const FIELD_APPLICATION_ID = 'applicationId';
export const FIELD_IP_PROTOCOL = 'ipProtocol';
export const FIELD_L7_PROTOCOL = 'l7ProtocolId';

export const FIELD_COUNTRY_ID = 'countryId';
export const FIELD_PROVINCE_ID = 'provinceId';
export const FIELD_CITY_ID = 'cityId';
export const FIELD_SOURCE_COUNTRY_ID = 'sourceCountryId';
export const FIELD_SOURCE_PROVINCE_ID = 'sourceProvinceId';
export const FIELD_SOURCE_CITY_ID = 'sourceCityId';
export const FIELD_DEST_COUNTRY_ID = 'destCountryId';
export const FIELD_DEST_PROVINCE_ID = 'destProvinceId';
export const FIELD_DEST_CITY_ID = 'destCityId';

const supportInOperatorFields = [FIELD_PORT, FIELD_SOURCE_PORT, FIELD_DEST_PORT, FIELD_VLANID];
const ipFields = [FIELD_IP, FIELD_DEST_IP, FIELD_SOURCE_IP];
const portFields = [FIELD_PORT, FIELD_SOURCE_PORT, FIELD_DEST_PORT];
export const FILTER_RULE_FIELD_LIST = [
  {
    label: 'IP(源IP或目的IP)',
    value: FIELD_IP,
  },
  {
    label: '源IP',
    value: FIELD_SOURCE_IP,
  },
  {
    label: '目的IP',
    value: FIELD_DEST_IP,
  },
  {
    label: '端口(源端口或目的端口)',
    value: FIELD_PORT,
  },
  {
    label: '源端口',
    value: FIELD_SOURCE_PORT,
  },
  {
    label: '目的端口',
    value: FIELD_DEST_PORT,
  },
  {
    label: 'MAC地址(源MAC或目的MAC)',
    value: FIELD_MAC,
  },
  {
    label: '源MAC地址',
    value: FIELD_SOURCE_MAC,
  },
  {
    label: '目的MAC地址',
    value: FIELD_DEST_MAC,
  },
  {
    label: '传输层协议',
    value: FIELD_IP_PROTOCOL,
  },
  {
    label: '应用层协议',
    value: FIELD_L7_PROTOCOL,
  },
  {
    label: 'VLANID',
    value: FIELD_VLANID,
  },
  {
    label: '应用',
    value: FIELD_APPLICATION_ID,
  },
  {
    label: '国家(源IP国家或目的IP国家)',
    value: FIELD_COUNTRY_ID,
  },
  {
    label: '省份(源IP省份或目的IP省份)',
    value: FIELD_PROVINCE_ID,
  },
  {
    label: '城市(源IP城市或目的IP城市)',
    value: FIELD_CITY_ID,
  },
  {
    label: '源IP国家',
    value: FIELD_SOURCE_COUNTRY_ID,
  },
  {
    label: '源IP省份',
    value: FIELD_SOURCE_PROVINCE_ID,
  },
  {
    label: '源IP城市',
    value: FIELD_SOURCE_CITY_ID,
  },
  {
    label: '目的IP国家',
    value: FIELD_DEST_COUNTRY_ID,
  },
  {
    label: '目的IP省份',
    value: FIELD_DEST_PROVINCE_ID,
  },
  {
    label: '目的IP城市',
    value: FIELD_DEST_CITY_ID,
  },
];
const ALL_FILTER_FIELD = FILTER_RULE_FIELD_LIST.map((item) => item.value);

const FILTER_RULE_OPERATOR_EQUAL = '=';
const FILTER_RULE_OPERATOR_UNEQUAL = '!=';
const FILTER_RULE_OPERATOR_IN = 'in';
const FILTER_RULE_OPERATOR_LIST = [
  {
    label: '等于',
    value: FILTER_RULE_OPERATOR_EQUAL,
  },
  {
    label: '不等于',
    value: FILTER_RULE_OPERATOR_UNEQUAL,
  },
  {
    label: '范围',
    value: FILTER_RULE_OPERATOR_IN,
  },
];

// 六元组抓包配置支持的协议类型
const FILTER_PROTOCOL_TYPE_LIST = [
  // {
  //   value: '',
  //   label: '不限制',
  // },
  {
    value: 'TCP',
    label: 'TCP',
  },
  {
    value: 'UDP',
    label: 'UDP',
  },
  {
    value: 'ICMP',
    label: 'ICMP',
  },
  {
    value: 'SCTP',
    label: 'SCTP',
  },
];

/** 根据value值查找名称 */
export const findNameByValue = (value: string, arr: any[]) => {
  if (!Array.isArray(arr)) {
    return '--';
  }
  const result = arr.find((item) => item.value === value);
  return result ? result.label : '--';
};

/** 移除不合适的的属性 */
export const removeInvalidRule = (ruleList: IFilterTuple[]) => {
  const newRuleList: IFilterTuple[] = [];
  ruleList.forEach((item) => {
    const obj = {};
    Object.keys(item).forEach((key) => {
      if (ALL_FILTER_FIELD.includes(key) && item[key]) {
        obj[key] = item[key];
      }
    });
    newRuleList.push(obj);
  });

  return newRuleList;
};

/** 判断某个字符是否禁用 */
const computedFieldIsDisabled = (field: string, usedFields: string[]) => {
  return (
    // usedFields.indexOf(field) > -1 ||
    // ip 存在时，源 ip 和目的 ip 不能再被选择了
    (usedFields.indexOf(FIELD_IP) > -1 && (field === FIELD_SOURCE_IP || field === FIELD_DEST_IP)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_IP, FIELD_DEST_IP]).length > 0 &&
      field === FIELD_IP) ||
    // port 存在时，源port 和目的port 不能再被选择了
    (usedFields.indexOf(FIELD_PORT) > -1 &&
      (field === FIELD_SOURCE_PORT || field === FIELD_DEST_PORT)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_PORT, FIELD_DEST_PORT]).length > 0 &&
      field === FIELD_PORT) ||
    // mac
    (usedFields.indexOf(FIELD_MAC) > -1 &&
      (field === FIELD_SOURCE_MAC || field === FIELD_DEST_MAC)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_MAC, FIELD_DEST_MAC]).length > 0 &&
      field === FIELD_MAC) ||
    // 国家
    (usedFields.indexOf(FIELD_COUNTRY_ID) > -1 &&
      (field === FIELD_SOURCE_COUNTRY_ID || field === FIELD_DEST_COUNTRY_ID)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_COUNTRY_ID, FIELD_DEST_COUNTRY_ID]).length > 0 &&
      field === FIELD_COUNTRY_ID) ||
    // 省份
    (usedFields.indexOf(FIELD_PROVINCE_ID) > -1 &&
      (field === FIELD_SOURCE_PROVINCE_ID || field === FIELD_DEST_PROVINCE_ID)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_PROVINCE_ID, FIELD_DEST_PROVINCE_ID]).length > 0 &&
      field === FIELD_PROVINCE_ID) ||
    // 城市
    (usedFields.indexOf(FIELD_CITY_ID) > -1 &&
      (field === FIELD_SOURCE_CITY_ID || field === FIELD_DEST_CITY_ID)) ||
    (_.intersection(usedFields, [FIELD_SOURCE_CITY_ID, FIELD_DEST_CITY_ID]).length > 0 &&
      field === FIELD_CITY_ID)
  );
};

/** 格式化规则条件 */
export const parseFilterTupleJson = (filterTupleString: string) => {
  let tupleList: IFilterTuple[] = [];
  try {
    tupleList = JSON.parse(filterTupleString);
    tupleList = removeInvalidRule(tupleList);
  } catch (err) {
    tupleList = [];
  }

  return tupleList;
};

interface IFilterTupleShareProps {
  value?: string;
  form: FormInstance;
  onChange?: (value: string) => void;
}

interface IFilterTupleProps extends IFilterTupleShareProps {
  applicationList: ApplicationItem[];
  allApplicationMap: IApplicationMap;
  allCountryList: ICountry[];
  allCountryMap: ICountryMap;
  allProvinceList: IProvince[];
  allProvinceMap: IProvinceMap;
  allCityList: ICity[];
  allCityMap: ICityMap;
  allL7ProtocolsList: IL7Protocol[];
  allL7ProtocolMap: IL7ProtocolMap;
  ruleProps?: any;
}
const FilterTuple: React.FC<IFilterTupleProps> = ({
  form,
  value,
  onChange,

  applicationList,
  allApplicationMap,
  allCountryList,
  allCountryMap,
  allProvinceList,
  allProvinceMap,
  allCityList,
  allCityMap,
  allL7ProtocolsList,
  allL7ProtocolMap,
  ruleProps,
}) => {
  const location = useLocation() as any as {
    query: {
      rules?: string;
    };
  };

  const { rules } = location.query;

  // 规则组
  const [filterTupleJson, setFilterTupleJson] = useState<IFilterTuple[]>([]);

  useEffect(() => {
    if (rules) {
      setFilterTupleJson((prev) => {
        return [...prev, ...(rules ? [JSON.parse(rules)] : [])];
      });
    } else if (ruleProps) {
      setFilterTupleJson((prev) => {
        return [...prev, ...[ruleProps]];
      });
    }
  }, [ruleProps, rules]);

  useEffect(() => {
    if (value) {
      const tupleList = parseFilterTupleJson(value);
      setFilterTupleJson((prev) => {
        return [...prev, ...tupleList];
      });
    }

    // 这里使用value作为deps会导致无限循环
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (onChange) {
      onChange(JSON.stringify(filterTupleJson));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterTupleJson]);

  /**
   * 新增规则组
   */
  const addTupleGroup = () => {
    setFilterTupleJson((prev) => {
      return [...prev, {}];
    });
  };

  /**
   * 删除规则组
   */
  const removeTupleGroup = (groupIndex: number) => {
    setFilterTupleJson((prev) => {
      return prev.filter((item, index) => index !== groupIndex);
    });
  };

  /**
   * 添加条件
   */
  const addTupleRule = (groupIndex: number) => {
    const fieldKey = `field_${groupIndex}`;
    const operatorKey = `operator_${groupIndex}`;
    const operandKey = `operand_${groupIndex}`;

    form.validateFields([fieldKey, operatorKey, operandKey]).then((values) => {
      const field = values[fieldKey];
      const operator = values[operatorKey];
      const operand = values[operandKey];

      // 更新数据源
      setFilterTupleJson((prev) => {
        const copy = _.cloneDeep(prev);
        if (!(copy[groupIndex][field] instanceof Array)) {
          copy[groupIndex][field] = [];
        }
        copy[groupIndex][field].push(
          operator === FILTER_RULE_OPERATOR_UNEQUAL ? `NOT_${operand}` : operand,
        );

        return copy;
      });

      // 更新表单
      form.setFieldsValue({
        [fieldKey]: undefined,
        [operatorKey]: undefined,
        [operandKey]: undefined,
      });
    });
  };

  /**
   * 删除某一个组下的规则
   */
  const removeTupleRule = (groupIndex: number, field: string, index?: number) => {
    if (index) {
      setFilterTupleJson((prev) => {
        const copy = _.cloneDeep(prev);
        copy[groupIndex][field].splice(index, 1);

        if (copy[groupIndex][field].length === 0) {
          delete copy[groupIndex][field];
        }
        return copy;
      });
    } else {
      setFilterTupleJson((prev) => {
        const copy = _.cloneDeep(prev);
        delete copy[groupIndex][field];

        return copy;
      });
    }
  };

  const handleFieldChange = (fieldId: string, groupIndex: number) => {
    form.setFieldsValue({
      [`field_${groupIndex}`]: fieldId,
      [`operator_${groupIndex}`]: FILTER_RULE_OPERATOR_EQUAL,
      [`operand_${groupIndex}`]: undefined,
    });
  };

  const handleOperatorChange = (operator: any, groupIndex: number) => {
    form.setFieldsValue({
      [`operand_${groupIndex}`]: undefined,
    });
  };

  const checkNumberRange = (arr: number[], min: number, max: number) => {
    let [one, two] = arr;
    one = one ? +one : -1;
    two = two ? +two : -1;
    if (arr.length === 1) {
      if (one < min || one > max) {
        throw new Error('不正确的范围');
      }
    }
    if (arr.length === 2) {
      if (one >= two) {
        throw new Error('不正确的范围');
      }
      if (one < min || one > max || two < min || two > max) {
        throw new Error('不正确的范围');
      }
    }
  };

  /** 校验操作符 */
  const checkOperator = (rule: any, operator: string, groupIndex: number) => {
    const field = form.getFieldValue(`field_${groupIndex}`);
    if (supportInOperatorFields.indexOf(field) === -1 && operator === FILTER_RULE_OPERATOR_IN) {
      throw new Error('只有端口和VLANDID支持范围查询');
    }
  };

  /** 校验操作数 */
  const checkOperand = (rule: any, operand: string, groupIndex: number) => {
    const field = form.getFieldValue(`field_${groupIndex}`);
    if (!field) {
      return;
    }

    if (!operand) {
      return;
    }

    const operator = form.getFieldValue(`operator_${groupIndex}`);
    const operandArr = operand ? operand.split('-') : [];
    if (operandArr.length === 0) {
      throw new Error('请输入内容值');
    }

    if (operator === FILTER_RULE_OPERATOR_EQUAL && operandArr.length !== 1) {
      throw new Error('内容值无法输入范围');
    }

    if (operator === FILTER_RULE_OPERATOR_IN) {
      if (operandArr.length !== 2) {
        throw new Error('错误的范围。请输入1-10这样的条件');
      }
      // 判断是否是数字
      if (isNaN(+operandArr[0]) || isNaN(+operandArr[1])) {
        throw new Error('错误的范围。请输入1-10这样的条件');
      }
    }

    // 校验 IP
    if (ipFields.indexOf(field) > -1) {
      if (operand.indexOf('/') > -1) {
        const splitList = operand.split('/');
        const ipAddress = splitList[0];
        const mask = +splitList[1];
        // 校验第一个 ip
        if (!ipV4Regex.test(ipAddress) && !ipV6Regex.test(ipAddress)) {
          throw new Error('请输入正确的IP/IP段。支持 IPv4 和 IPv6');
        }
        // 校验子网掩码
        if (!mask || isNaN(mask)) {
          throw new Error('IP网段请填写子网掩码');
        }
        // IPv4最高支持32
        if (ipV4Regex.test(ipAddress) && (mask <= 0 || mask > 32)) {
          throw new Error('IPv4子网掩码范围是(0,32]');
        }
        // IPv6最高支持128
        if (ipV6Regex.test(ipAddress) && (mask <= 0 || mask > 128)) {
          throw new Error('IPv6子网掩码范围是(0,128]');
        }
      } else if (!ipV4Regex.test(operand) && !ipV6Regex.test(operand)) {
        throw new Error('请输入正确的IP地址');
      }
      return;
    }

    if (portFields.indexOf(field) > -1) {
      checkNumberRange(operandArr as any, PORT_MIN_NUMBER, PORT_MAX_NUMBER);
      return;
    }

    if (field === FIELD_VLANID) {
      checkNumberRange(operandArr as any, VLANID_MIN_NUMBER, VLANID_MAX_NUMBER);
    }
  };

  /** 根据字段计判断显示字段的显示标签 */
  const renderOperandText = (field: string, ruleGroup: IFilterTuple, index?: number) => {
    const targetValue = index !== undefined ? ruleGroup[field][index] : ruleGroup[field];
    if (field === FIELD_APPLICATION_ID) {
      return allApplicationMap[targetValue] ? allApplicationMap[targetValue].nameText : targetValue;
    }
    if (field === FIELD_L7_PROTOCOL) {
      return allL7ProtocolMap[targetValue] ? allL7ProtocolMap[targetValue].nameText : targetValue;
    }
    if (
      field === FIELD_COUNTRY_ID ||
      field === FIELD_SOURCE_COUNTRY_ID ||
      field === FIELD_DEST_COUNTRY_ID
    ) {
      return allCountryMap[targetValue] ? allCountryMap[targetValue].fullName : targetValue;
    }
    if (
      field === FIELD_PROVINCE_ID ||
      field === FIELD_SOURCE_PROVINCE_ID ||
      field === FIELD_DEST_PROVINCE_ID
    ) {
      return allProvinceMap[targetValue] ? allProvinceMap[targetValue].fullName : targetValue;
    }
    if (field === FIELD_CITY_ID || field === FIELD_SOURCE_CITY_ID || field === FIELD_DEST_CITY_ID) {
      return allCityMap[targetValue] ? allCityMap[targetValue].fullName : targetValue;
    }
    return targetValue;
  };

  /** 根据字段判断显示操作数的DOM */
  const renderOperand = (field: string) => {
    if (field === FIELD_APPLICATION_ID) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择应用"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {applicationList.map((app) => (
            <Select.Option key={app.applicationId} value={app.applicationId}>
              {app.nameText}
            </Select.Option>
          ))}
        </Select>
      );
    }
    // 国家
    if (
      field === FIELD_COUNTRY_ID ||
      field === FIELD_SOURCE_COUNTRY_ID ||
      field === FIELD_DEST_COUNTRY_ID
    ) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择国家"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allCountryList.map((item) => (
            <Select.Option key={item.countryId} value={item.countryId}>
              {item.nameText}
            </Select.Option>
          ))}
        </Select>
      );
    }
    // 省份
    if (
      field === FIELD_PROVINCE_ID ||
      field === FIELD_SOURCE_PROVINCE_ID ||
      field === FIELD_DEST_PROVINCE_ID
    ) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择省份"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allProvinceList.map((item) => (
            <Select.Option key={item.provinceId} value={item.provinceId}>
              {item.fullName}
            </Select.Option>
          ))}
        </Select>
      );
    }
    // 城市
    if (field === FIELD_CITY_ID || field === FIELD_SOURCE_CITY_ID || field === FIELD_DEST_CITY_ID) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择城市"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allCityList.map((item) => (
            <Select.Option key={item.cityId} value={item.cityId}>
              {item.fullName}
            </Select.Option>
          ))}
        </Select>
      );
    }

    if (field === FIELD_IP_PROTOCOL) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择传输层协议"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {FILTER_PROTOCOL_TYPE_LIST.map(({ label, value: protocolType }) => (
            <Select.Option key={protocolType} value={protocolType}>
              {label}
            </Select.Option>
          ))}
        </Select>
      );
    }
    if (field === FIELD_L7_PROTOCOL) {
      return (
        <Select
          disabled={!field}
          style={{ width: 200 }}
          showSearch
          placeholder="请选择应用层协议"
          filterOption={(input, option) =>
            option?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          {allL7ProtocolsList.map((protocol) => (
            <Select.Option key={protocol.protocolId} value={protocol.protocolId}>
              {protocol.nameText}
            </Select.Option>
          ))}
        </Select>
      );
    }

    return <Input disabled={!field} style={{ width: 200 }} placeholder="填写内容值" />;
  };

  return (
    <>
      <Form.Item name="filter-tuple" extra={filterTupleExtra} style={{ marginBottom: 0 }}>
        <Card bordered bodyStyle={{ padding: 4 }}>
          {filterTupleJson.map((ruleGroup, groupIndex) => {
            const usedFields = Object.keys(ruleGroup);
            // ip 被使用了时，三个 ip 不能被选择
            // 端口被使用了时，三个端口字段不能被选择
            const tmpRuleGroup = (() => {
              const res: any = {};
              Object.keys(ruleGroup).map((key) => {
                if (!Array.isArray(ruleGroup[key])) {
                  res[key] = ruleGroup[key].replace('NOT_', '');
                  return;
                }
                res[key] = ruleGroup[key].map((item: any) => item.replace('NOT_', ''));
              });
              return res;
            })();

            return (
              <Card
                bodyStyle={{ padding: 10 }}
                style={{ marginBottom: 10 }}
                // eslint-disable-next-line react/no-array-index-key
                key={JSON.stringify(ruleGroup) + groupIndex}
              >
                <div className={styles.filterWrap}>
                  <Form.Item noStyle shouldUpdate>
                    {({ getFieldValue }) => {
                      const selectedField = getFieldValue(`field_${groupIndex}`);
                      const operator = getFieldValue(`operator_${groupIndex}`);
                      const operand = getFieldValue(`operand_${groupIndex}`);

                      return (
                        <div className={styles.filterForm}>
                          <Input.Group compact>
                            <Form.Item name={`field_${groupIndex}`}>
                              <Select
                                style={{ width: 200 }}
                                placeholder="选择字段"
                                onChange={(fieldId) =>
                                  handleFieldChange(fieldId as any, groupIndex)
                                }
                              >
                                {FILTER_RULE_FIELD_LIST.map(({ label, value: fieldId }) => (
                                  <Select.Option
                                    key={fieldId}
                                    disabled={computedFieldIsDisabled(fieldId, usedFields)}
                                    value={fieldId}
                                  >
                                    {label}
                                  </Select.Option>
                                ))}
                              </Select>
                            </Form.Item>
                            <Form.Item
                              name={`operator_${groupIndex}`}
                              initialValue={FILTER_RULE_OPERATOR_EQUAL}
                              shouldUpdate
                              rules={[
                                {
                                  validator: async (rule, val) =>
                                    checkOperator(rule, val, groupIndex),
                                },
                              ]}
                            >
                              <Select
                                disabled={!selectedField}
                                style={{ width: 100 }}
                                placeholder="操作方法"
                                onChange={(val) => handleOperatorChange(val as any, groupIndex)}
                              >
                                {FILTER_RULE_OPERATOR_LIST.slice(
                                  0,
                                  supportInOperatorFields.indexOf(selectedField) > -1
                                    ? FILTER_RULE_OPERATOR_LIST.length
                                    : FILTER_RULE_OPERATOR_LIST.length - 1,
                                ).map(({ label, value: _value }) => (
                                  <Select.Option key={value} value={_value}>
                                    {label}
                                  </Select.Option>
                                ))}
                              </Select>
                            </Form.Item>
                            <Form.Item
                              shouldUpdate
                              name={`operand_${groupIndex}`}
                              rules={[
                                {
                                  validator: async (rule, val) =>
                                    checkOperand(rule, val, groupIndex),
                                },
                              ]}
                            >
                              {renderOperand(selectedField)}
                            </Form.Item>
                          </Input.Group>
                          <Input.Group>
                            <Form.Item>
                              <Button
                                size="small"
                                type="primary"
                                disabled={!(selectedField && operator && operand)}
                                icon={<PlusOutlined />}
                                style={{ marginRight: 10, marginLeft: 10 }}
                                onClick={() => addTupleRule(groupIndex)}
                              >
                                添加条件
                              </Button>
                              <Popconfirm
                                title="确定删除这组规则吗？"
                                onConfirm={() => removeTupleGroup(groupIndex)}
                              >
                                <Button type="primary" size="small" danger icon={<CloseOutlined />}>
                                  删除规则
                                </Button>
                              </Popconfirm>
                            </Form.Item>
                          </Input.Group>
                        </div>
                      );
                    }}
                  </Form.Item>

                  <div className={styles.filterContent}>
                    {Object.keys(ruleGroup).map((el) => {
                      if (typeof ruleGroup[el] === 'string') {
                        if (ruleGroup[el] === '') {
                          return null;
                        }
                        const opText = renderOperandText(el, ruleGroup).split('NOT_');
                        const tmpOperator = opText.length > 1 ? '!=' : '=';
                        return (
                          <Tag
                            color="blue"
                            closable
                            key={`${el}_${ruleGroup[el]}`}
                            onClose={() => removeTupleRule(groupIndex, el)}
                          >
                            {findNameByValue(el, FILTER_RULE_FIELD_LIST)}
                            {tmpOperator}
                            {renderOperandText(el, tmpRuleGroup)}
                          </Tag>
                        );
                      }
                      return (ruleGroup[el] as any[]).map((v, index) => {
                        const opText = renderOperandText(el, ruleGroup, index).split('NOT_');
                        const tmpOperator = opText.length > 1 ? '!=' : '=';
                        return (
                          <Tag
                            color="blue"
                            closable
                            key={`${el}_${v}`}
                            onClose={() => removeTupleRule(groupIndex, el, index)}
                          >
                            {findNameByValue(el, FILTER_RULE_FIELD_LIST)}
                            {tmpOperator}
                            {renderOperandText(el, tmpRuleGroup, index)}
                          </Tag>
                        );
                      });
                    })}
                  </div>
                </div>
              </Card>
            );
          })}
          <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
            {filterTupleJson.length < TUPLE_MAX_COUNT ? (
              <Button type="primary" size="small" onClick={addTupleGroup}>
                <PlusOutlined /> 添加规则
              </Button>
            ) : (
              <Button type="primary" size="small" disabled>
                最多可配置{TUPLE_MAX_COUNT}个
              </Button>
            )}
          </Form.Item>
        </Card>
      </Form.Item>
    </>
  );
};

export default connect(
  ({
    SAKnowledgeModel: { applicationList, allApplicationMap },
    metadataModel: { allL7ProtocolsList, allL7ProtocolMap },
    geolocationModel: {
      allCountryList,
      allCountryMap,
      allProvinceList,
      allProvinceMap,
      allCityList,
      allCityMap,
    },
  }: ConnectState) => ({
    applicationList,
    allApplicationMap,
    allL7ProtocolsList,
    allL7ProtocolMap,
    allCountryList,
    allCountryMap,
    allProvinceList,
    allProvinceMap,
    allCityList,
    allCityMap,
  }),
)(FilterTuple);
