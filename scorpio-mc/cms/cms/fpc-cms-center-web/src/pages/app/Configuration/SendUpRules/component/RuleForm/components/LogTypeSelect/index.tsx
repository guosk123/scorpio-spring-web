import FieldFilter, { getFilterContent } from '@/components/FieldFilter';
import type { IField, IFilter, IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import { Alert, Badge, Button, Card, Col, Divider, Modal, Row, Select, Space, Tag } from 'antd';
import type { ReactNode } from 'react';
import { useEffect } from 'react';
import { useMemo } from 'react';
import { useState } from 'react';
import {
  METADATA_TABLE_MAP,
  NETWORK_ALERT_KEY,
  NETWORK_ALERT_PROPERTIES,
  SERVICE_ALERT_KEY,
  SERVICE_ALERT_PROPERTIES,
  SYSTEM_ALERT_KEY,
  SYSTEM_ALERT_PROPERTIES,
  SYSTEM_LOG_KEY,
  SYSTEM_LOG_PROPERTIES,
} from '../../dict';
import type { IProperty } from '../../typing';
import { snakeCaseIgnoreNumber } from '../../utils';
import useLogTypeField from './hooks/useLogTypeField';

interface Props {
  /** 选择器标题 */
  title: string;
  /** 选择器唯一key */
  index: string;
  /** 加载状态 */
  loading?: boolean;
  /** 更新logtype信息 */
  updateLogTypeInfo: (
    index: string,
    properties?: IProperty[],
    conditions?: IFilter[],
    dsl?: string,
  ) => void;
  /** 获得connditions  */
  getLogTypeConditions: (index: string) => IFilter[];
  /** 获得properties */
  getLogTypeProperties: (index: string) => IProperty[];
  /** 数据源 */
  dataSource: IProperty[];
}

const { Option } = Select;

/** 渲染属性Option */
const renderPropertyOption = (index: string, dataSource: IProperty[]) => {
  if (index === NETWORK_ALERT_KEY) {
    return Object.keys(NETWORK_ALERT_PROPERTIES).map((k: string) => {
      const data = NETWORK_ALERT_PROPERTIES[k];
      return <Option key={JSON.stringify(data)}>{data.comment || data.name}</Option>;
    });
  } else if (index === SERVICE_ALERT_KEY) {
    return Object.keys(SERVICE_ALERT_PROPERTIES).map((k: string) => {
      const data = SERVICE_ALERT_PROPERTIES[k];
      return <Option key={JSON.stringify(data)}>{data.comment || data.name}</Option>;
    });
  } else if (index === SYSTEM_ALERT_KEY) {
    return Object.keys(SYSTEM_ALERT_PROPERTIES).map((k: string) => {
      const data = SYSTEM_ALERT_PROPERTIES[k];
      return <Option key={JSON.stringify(data)}>{data.comment || data.name}</Option>;
    });
  } else if (index === SYSTEM_LOG_KEY) {
    return Object.keys(SYSTEM_LOG_PROPERTIES).map((k: string) => {
      const data = SYSTEM_LOG_PROPERTIES[k];
      return <Option key={JSON.stringify(data)}>{data.comment || data.name}</Option>;
    });
  } else {
    return dataSource?.map((data: any) => {
      return <Option key={JSON.stringify(data)}>{data.comment || data.name}</Option>;
    });
  }
};

export default function LogTypeSelect({
  title,
  index,
  loading = false,
  getLogTypeProperties,
  updateLogTypeInfo,
  getLogTypeConditions,
  dataSource,
}: Props) {
  const fullColumns = useLogTypeField<any[]>({ index });

  const filterFields = useMemo(() => {
    if (index === 'dhcp_v6') {
      return (fullColumns.filter((f) => f.searchable) as IField[])
        .filter((f) => !f?.dataIndex?.includes('Ipv6'))
        .map((item) => {
          return {
            ...item,
            dataIndex: snakeCaseIgnoreNumber(item.dataIndex),
          };
        });
    }

    if(index === 'suricata') {
      return (fullColumns.filter((f) => f.searchable) as IField[])
        .filter((f) => !f?.dataIndex?.includes('networkId')&&!f?.dataIndex?.includes('polictName'))
        .map((item) => {
          return {
            ...item,
            dataIndex: snakeCaseIgnoreNumber(item.dataIndex),
          };
        });
    }

    return (fullColumns.filter((f) => f.searchable) as IField[]).map((item) => {
      let operandType = item?.operandType;
      if (item.operandType === EFieldOperandType.IP) {
        operandType = EFieldOperandType.SINGLE_IP;
      }
      if (item.operandType === EFieldOperandType.IPV4) {
        operandType = EFieldOperandType.SINGLE_IPV4;
      }
      if (item.operandType === EFieldOperandType.IPV6) {
        operandType = EFieldOperandType.SINGLE_IPV6;
      }
      return {
        ...item,
        operandType,
        dataIndex: snakeCaseIgnoreNumber(item.dataIndex),
      };
    });
  }, [fullColumns, index]);

  const [modalVisible, setModalVisible] = useState<boolean>(false);

  const [filterCondition, setFilterCondition] = useState<IFilterCondition>(
    getLogTypeConditions(index),
  );

  useEffect(() => {
    updateLogTypeInfo(
      index,
      undefined,
      filterCondition as IFilter[],
      filterCondition.reduce((prev, cur) => {
        let res = '';
        if (prev !== '') {
          res += ' AND ';
        }
        const newFilter = {
          ...(cur as IFilter),
          ...(() => {
            if ((cur as IFilter)?.field === 'l7_protocol_id') {
              return { operand: parseInt((cur as IFilter)?.operand as string) };
            }
            return {};
          })(),
        };
        res += `( ${getFilterContent(newFilter, true, filterFields)} )`;
        return prev + res;
      }, ''),
    );
  }, [filterCondition]);

  /** 渲染过滤器 */
  const renderPropertyFilter = () => {
    return (
      <span
        style={{ display: 'inline-block' }}
        onClick={(e) => {
          e.stopPropagation();
        }}
      >
        <FieldFilter
          fields={filterFields}
          historyStorageKey={`metadata-filter-history`}
          simple
          hideSave={true}
          condition={filterCondition}
          onChange={(c) => {
            setFilterCondition(c);
          }}
        />
      </span>
    );
  };

  return (
    <>
      <Badge.Ribbon text={title} placement="start">
        <Card
          hoverable
          style={{ cursor: 'default' }}
          onClick={(e) => {
            e.stopPropagation();
          }}
          headStyle={{ border: '0px', paddingBottom: '0px' }}
          bodyStyle={{ marginTop: '20px' }}
          size="small"
        >
          <Row style={{ width: '100%' }}>
            <Col span={21}>
              <Select
                style={{ width: '100%' }}
                mode="multiple"
                onClick={(e) => {
                  e.stopPropagation();
                }}
                onChange={(p) => {
                  updateLogTypeInfo(
                    index,
                    p.map((d) => JSON.parse(d)),
                    undefined,
                  );
                }}
                value={getLogTypeProperties(index).map((d) => JSON.stringify(d))}
                placeholder="请选择外发字段(默认勾选所有字段)"
                loading={loading}
                disabled={loading}
                maxTagCount="responsive"
                dropdownRender={(menu: ReactNode) => {
                  return (
                    <>
                      {menu}
                      <Divider style={{ margin: '8px 0' }} />
                      <Space style={{ padding: '0 8px 4px' }}>
                        <Button
                          type="link"
                          size="small"
                          onClick={() => {
                            if (index === NETWORK_ALERT_KEY) {
                              updateLogTypeInfo(
                                index,
                                Object.keys(NETWORK_ALERT_PROPERTIES).map(
                                  (k) => NETWORK_ALERT_PROPERTIES[k],
                                ),
                                undefined,
                              );
                            } else if (index === SERVICE_ALERT_KEY) {
                              updateLogTypeInfo(
                                index,
                                Object.keys(SERVICE_ALERT_PROPERTIES).map(
                                  (k) => SERVICE_ALERT_PROPERTIES[k],
                                ),
                                undefined,
                              );
                            } else if (index === SYSTEM_ALERT_KEY) {
                              updateLogTypeInfo(
                                index,
                                Object.keys(SYSTEM_ALERT_PROPERTIES).map(
                                  (k) => SYSTEM_ALERT_PROPERTIES[k],
                                ),
                                undefined,
                              );
                            } else if (index === SYSTEM_LOG_KEY) {
                              updateLogTypeInfo(
                                index,
                                Object.keys(SYSTEM_LOG_PROPERTIES).map(
                                  (k) => SYSTEM_LOG_PROPERTIES[k],
                                ),
                                undefined,
                              );
                            } else {
                              updateLogTypeInfo(index, dataSource, undefined);
                            }
                          }}
                        >
                          全选
                        </Button>
                        <Button
                          type="link"
                          size="small"
                          onClick={() => {
                            updateLogTypeInfo(index, [], undefined);
                          }}
                        >
                          取消选中
                        </Button>
                      </Space>
                    </>
                  );
                }}
              >
                {renderPropertyOption(index, dataSource)}
              </Select>
            </Col>
            <Col>
              {METADATA_TABLE_MAP[index] || index === 'suricata' || index === 'flowlog' ? (
                <Button
                  type="link"
                  onClick={() => {
                    setModalVisible(true);
                  }}
                >
                  添加过滤条件
                </Button>
              ) : (
                ''
              )}
            </Col>
          </Row>
          <Row>
            <Col span={23}>
              {(() => {
                if (!getLogTypeConditions(index)) {
                  return '';
                }
                if (getLogTypeConditions(index)?.length > 0) {
                  return (
                    <Alert
                      description={
                        <>
                          {getLogTypeConditions(index).map((c) => {
                            return (
                              <Tag
                                key={c.id}
                                closable
                                onClose={() => {
                                  const conditions = getLogTypeConditions(index);
                                  setFilterCondition(conditions.filter((f) => f.id !== c.id));
                                }}
                              >
                                {filterFields ? getFilterContent(c, false, filterFields) : ''}
                              </Tag>
                            );
                          })}
                        </>
                      }
                      type="info"
                      style={{ marginTop: '10px', padding: '5px' }}
                    />
                  );
                }
                return '';
              })()}
            </Col>
          </Row>
        </Card>
      </Badge.Ribbon>
      <div
        onClick={(e) => {
          e.stopPropagation();
        }}
      >
        <Modal
          visible={modalVisible}
          title={`添加${title}过滤条件`}
          closable={false}
          width={'50%'}
          onCancel={(e) => {
            e.stopPropagation();
          }}
          footer={
            <>
              <Button
                onClick={(e) => {
                  e.stopPropagation();
                  setModalVisible(false);
                }}
              >
                关闭
              </Button>
            </>
          }
        >
          {renderPropertyFilter()}
        </Modal>
      </div>
    </>
  );
}
