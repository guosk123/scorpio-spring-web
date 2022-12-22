import { parseArrayJson } from '@/utils/utils';
import { CloseOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Popconfirm, Select, Tag } from 'antd';
import type { FormInstance } from 'antd/es/form/Form';
import _ from 'lodash';
import React, { useEffect, useState } from 'react';
import { v1 as uuidv1 } from 'uuid';
import type { IFilterRaw, IFilterRawRule } from '../../typings';
import { EFilterRawType, FILTER_RAW_TYPE_MAP } from '../../typings';
import styles from './index.less';

const FormItem = Form.Item;
const { Option } = Select;

/**
 * 最多支持10组规则
 */
const RULE_MAX_COUNT = 10;
/**
 * 规则中最多支持5个条件
 */
const CONDITION_MAX_COUNT = 5;

/**
 * 签名类型
 */
const FILTER_RAW_TYPE_LIST = Object.keys(FILTER_RAW_TYPE_MAP).map((key) => ({
  value: key,
  label: FILTER_RAW_TYPE_MAP[key],
}));

/**
 * 给每个规则添加ID
 * @param rules 原始的规则条件
 *
 * ```json
 * [[{type: 'ascii', value: 'xxxx'}]] => [{id: uuid, group: [{id: uuid, type: 'ascii', value: 'xxxx'}]}]
 * ```
 */
export function formatterRule(rules: IFilterRaw[][] = []): IFilterRawRule[] {
  const nextRule: IFilterRawRule[] = [];
  for (let i = 0; i < rules.length; i += 1) {
    const group = {
      id: uuidv1(),
      group: rules[i].map((item) => ({ ...item, id: uuidv1() })),
    };
    nextRule.push(group);
  }

  return nextRule;
}

/** 格式化内容过滤 */
export const parseFilterRawJson = (filterRawString: string) => {
  return formatterRule(parseArrayJson(filterRawString));
};

export const extraDesc = (
  <section>
    <ul style={{ listStyle: 'decimal', paddingLeft: 20, marginBottom: 0 }}>
      <li>最多可以配置{RULE_MAX_COUNT}组有效规则</li>
      <li>每组规则中最多可以配置{CONDITION_MAX_COUNT}个条件</li>
      <li>规则组与规则组之间为[或]的关系</li>
      <li>规则组内的条件之间为[与]的关系</li>
    </ul>
  </section>
);
function isReg(r: string) {
  try {
    new RegExp(r);
  } catch {
    return false;
  }
  return true;
}
interface IFilterRawProps {
  value?: string;
  onChange?: (value: string) => void;
  readonly?: boolean;
  form: FormInstance<any>;
  describeMode?: boolean;
  smallSize?: boolean;
}

const FilterRaw: React.FC<IFilterRawProps> = ({
  value,
  onChange,
  readonly = false,
  form,
  describeMode,
  smallSize = false,
}) => {
  const [rules, setRules] = useState<IFilterRawRule[]>([]);

  useEffect(() => {
    if (value) {
      setRules(parseFilterRawJson(value));
    }
    return () => {
      setRules([]);
    };
  }, []);

  useEffect(() => {
    const nextRules: IFilterRaw[][] = [];
    for (let index = 0; index < rules.length; index += 1) {
      const { group = [] } = rules[index];
      const nextGroup = group.map((item) => ({ type: item.type, value: item.value }));
      nextRules.push(nextGroup);
    }
    if (onChange) {
      onChange(JSON.stringify(nextRules));
    }
  }, [rules]);

  /**
   * 添加规则组
   */
  const handleAddRule = () => {
    setRules((prevValues) => [...prevValues, { id: uuidv1(), group: [] }]);
  };

  const handleRemoveRule = (groupId: string) => {
    setRules((prevRules) => prevRules.filter((group) => group.id !== groupId));
  };

  const handleTypeChange = (groupId: string) => {
    // 清空原有的数据
    form.setFieldsValue({
      [`group_${groupId}_value`]: undefined,
    });
  };

  /**
   * 某个规则组内添加条件
   * @param groupId 规则组ID
   */
  const addCondition = (groupId: string) => {
    const fieldType = `group_${groupId}_type`;
    const fieldValue = `group_${groupId}_value`;
    form.validateFields([fieldType, fieldValue]).then((fields) => {
      const copyRules = _.cloneDeep(rules);
      const group = copyRules.find((item) => item.id === groupId);

      if (group) {
        // 获取form表单里面的值
        group.group.push({
          id: uuidv1(),
          type: fields[fieldType],
          value: fields[fieldValue],
        });

        setRules(copyRules);

        // 清空原始内容
        form.setFieldsValue({
          [fieldValue]: undefined,
        });
      }
    });
  };

  /**
   * 删除条件
   * @param groupId 规则组ID
   * @param id 条件ID
   */
  const removeCondition = (groupId: string, id: string) => {
    const copyRules = _.cloneDeep(rules);
    const group = copyRules.find((el) => el.id === groupId);
    if (!group) {
      return;
    }

    group.group = group.group.filter((el) => el.id !== id);
    setRules(copyRules);
  };

  return (
    <>
      <Form.Item name="filter-raw" extra={describeMode && extraDesc} style={{ marginBottom: 0 }}>
        <Card bodyStyle={{ padding: 4 }}>
          <div className={styles.content}>
            {rules.map(({ id: groupId, group = [] }) => {
              return (
                <Card bodyStyle={{ padding: 10 }} style={{ marginBottom: 10 }} key={groupId}>
                  <div className={styles.filterForm} style={{ display: 'flex' }}>
                    <Input.Group compact style={smallSize ? { flex: '0 0 70%' } : {}}>
                      <FormItem name={`group_${groupId}_type`} initialValue={EFilterRawType.ASCII}>
                        <Select
                          style={{ width: 100 }}
                          onChange={(value) => {
                            handleTypeChange(groupId);
                            const elem = document.getElementById(`id_${groupId}`);
                            if (elem) {
                              if (value === EFilterRawType.CHINESE) {
                                // @ts-ignore
                                elem.innerText = '中文支持按照UTF-8，GB2312两种编码进行匹配';
                              } else {
                                // @ts-ignore
                                elem.innerText = '';
                              }
                            }
                          }}
                        >
                          {FILTER_RAW_TYPE_LIST.map(({ label, value }) => (
                            <Option
                              key={value}
                              value={value}
                              disabled={(() => {
                                if (group.length > 0) {
                                  const hasReg = group.some(
                                    (g) => g.type === EFilterRawType.REGULAR,
                                  );
                                  if (hasReg && value !== EFilterRawType.REGULAR) {
                                    return true;
                                  }
                                  if (!hasReg && value === EFilterRawType.REGULAR) {
                                    return true;
                                  }
                                }
                                return false;
                              })()}
                            >
                              {label}
                            </Option>
                          ))}
                        </Select>
                      </FormItem>
                      <FormItem
                        name={`group_${groupId}_value`}
                        initialValue={undefined}
                        rules={[
                          {
                            validator: async (rule, value) => {
                              if (!value) {
                                // TODO: 增加校验
                                return;
                                //   throw new Error('请填写匹配内容');
                              }
                              const type = form.getFieldValue([`group_${groupId}_type`]);
                              if (type === EFilterRawType.ASCII) {
                                if (value.length > 64) {
                                  throw new Error('最大长度可输入64');
                                }
                                // ASCII可见字符+中文
                                // 包括空格
                                if (!/[\x20-\x7e]+$/.test(value)) {
                                  throw new Error('请输入可见的ASCII字符');
                                }
                                return;
                              }

                              if (type === EFilterRawType.HEX) {
                                // 支持三种输入：0-9，a-f, A-F
                                if (value.length % 2 !== 0) {
                                  throw new Error('请输入偶数位字符串');
                                }
                                if (value.length > 128) {
                                  throw new Error('最大长度可输入128');
                                }
                                if (!/(0x)?[0-9a-f]+$/i.test(value)) {
                                  throw new Error('请输入正确的十六进制字符');
                                }
                              }

                              if (type === EFilterRawType.CHINESE) {
                                let count = 0;
                                if (value) {
                                  for (let i = 0; i < value.length; i++) {
                                    if (value.charCodeAt(i) > 255) {
                                      count += 3;
                                    } else {
                                      count++;
                                    }
                                  }
                                }
                                if (count > 60) {
                                  throw new Error('输入长度超过限制 ');
                                }
                              }

                              if (type === EFilterRawType.REGULAR) {
                                if (!isReg(value)) {
                                  throw new Error('正则表达式格式错误');
                                }
                              }
                            },
                          },
                        ]}
                      >
                        <Input
                          style={smallSize ? { width: 200 } : { width: 300 }}
                          placeholder="请填写匹配内容"
                        />
                      </FormItem>
                    </Input.Group>
                    <Input.Group>
                      <FormItem shouldUpdate noStyle>
                        {({ getFieldValue }) => (
                          <FormItem>
                            <div style={{ display: 'flex' }}>
                              <Button
                                size="small"
                                type="primary"
                                icon={<PlusOutlined />}
                                style={smallSize ? { marginRight: 2 } : { marginRight: 10 }}
                                disabled={!getFieldValue([`group_${groupId}_value`])}
                                onClick={() => addCondition(groupId)}
                              >
                                {smallSize ? '添加' : '添加条件'}
                              </Button>
                              <Popconfirm
                                title="确定删除这组规则吗？"
                                onConfirm={() => handleRemoveRule(groupId)}
                              >
                                <Button type="primary" size="small" danger icon={<CloseOutlined />}>
                                  {smallSize ? '删除' : '删除规则'}
                                </Button>
                              </Popconfirm>
                            </div>
                          </FormItem>
                        )}
                      </FormItem>
                    </Input.Group>
                  </div>
                  {<div style={{ color: 'rgba(0,0,0,0.45)' }} id={`id_${groupId}`} />}
                  <div className={styles.filterContent}>
                    {group.map((el) => (
                      <Tag
                        color="blue"
                        closable
                        key={el.id}
                        onClose={() => removeCondition(groupId, el.id!)}
                      >
                        {FILTER_RAW_TYPE_MAP[el.type]}={el.value}
                      </Tag>
                    ))}
                  </div>
                </Card>
              );
            })}
          </div>
          <div className={styles.operate}>
            <Form.Item style={{ marginBottom: 0, textAlign: 'center' }}>
              {rules.length < RULE_MAX_COUNT ? (
                <Button type="primary" size="small" onClick={handleAddRule}>
                  <PlusOutlined /> 添加规则
                </Button>
              ) : (
                <Button type="primary" size="small" disabled>
                  最多可配置{RULE_MAX_COUNT}个
                </Button>
              )}
            </Form.Item>
          </div>
        </Card>
      </Form.Item>
    </>
  );
};

export default FilterRaw;
