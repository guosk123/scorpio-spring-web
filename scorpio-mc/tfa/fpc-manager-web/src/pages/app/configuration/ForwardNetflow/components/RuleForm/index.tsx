import type { ConnectState } from '@/models/connect';
import { MinusCircleOutlined } from '@ant-design/icons';
import {
  Button,
  Card,
  Col,
  Divider,
  Form,
  Input,
  Radio,
  Row,
  Select,
  Space,
  TreeSelect,
} from 'antd';
import { useEffect, useMemo } from 'react';
import type { Dispatch, SAKnowledgeModelState } from 'umi';
import { history, useDispatch, useSelector } from 'umi';
import { exceptTupleExtra } from '../../../IngestPolicy/components/IngestPolicyForm';
import { createForwardRule, updateForwardRule } from '../../service';
import type { IForwardRule } from '../../typings';
import { EForwardRuleAction } from '../../typings';
import { checkPort, checkSourceOrDestIp, checkVlan } from '../common';

const MAX_TUPLE_COUNT = 10;

type ApplicationIdItem = {
  categoryId: string;
  subCategoryId: string | null;
  applicationId: string | null;
};

// 规则抓包配置支持的协议类型
const FILTER_PROTOCOL_TYPE_LIST = [
  {
    key: '',
    label: '不限制',
  },
  {
    key: 'TCP',
    label: 'TCP',
  },
  {
    key: 'UDP',
    label: 'UDP',
  },
];

interface Props {
  detail?: IForwardRule;
}

const RuleForm = (props: Props) => {
  const { detail } = props;
  const [form] = Form.useForm();

  const dispatch = useDispatch<Dispatch>();
  const { allCategoryList } = useSelector<ConnectState, SAKnowledgeModelState>(
    (state) => state.SAKnowledgeModel,
  );

  useEffect(() => {
    dispatch({
      type: 'saKnowledgeModel/queryAllApplications',
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const appTree = useMemo(() => {
    return allCategoryList.map((category) => {
      return {
        title: category.nameText,
        value: category.categoryId,
        key: category.categoryId,
        children: category.subCategoryList?.map((subCategory) => {
          return {
            title: subCategory.nameText,
            value: `${category.categoryId}-${subCategory.subCategoryId}`,
            key: `${category.categoryId}-${subCategory.subCategoryId}`,
            children: subCategory.applicationList?.map((app) => {
              return {
                title: app.nameText,
                value: `${category.categoryId}-${subCategory.subCategoryId}-${app.applicationId}`,
                key: `${category.categoryId}-${subCategory.subCategoryId}-${app.applicationId}`,
              };
            }),
          };
        }),
      };
    });
  }, [allCategoryList]);

  useEffect(() => {
    if (detail) {
      const tuples = detail.exceptTuple ? (JSON.parse(detail.exceptTuple) as any[]) : [];
      tuples.forEach((item, index) => {
        const { applicationId: applicationIds } = item;
        const appIds = (applicationIds as ApplicationIdItem[]).map((idItem) => {
          const { categoryId, subCategoryId, applicationId } = idItem;
          return `${categoryId}${subCategoryId ? `-${subCategoryId}` : ''}${
            applicationId ? `-${applicationId}` : ''
          }`;
        });
        tuples[index].applicationId = appIds;
      });
      form.setFieldsValue({
        ...detail,
        exceptTuple: tuples,
      });
    }
  }, [detail, form]);

  const handleFinish = (values: any) => {
    const { exceptTuple } = values;
    const tuples = ((exceptTuple as any[]) || []).map((item) => {
      const { applicationId } = item;
      const appIds = ((applicationId as string[]) || [])?.map((idSplitByMinus) => {
        const [categoryId, subCategoryId, appId] = idSplitByMinus.split('-');
        return {
          categoryId: categoryId,
          subCategoryId: subCategoryId ? subCategoryId : null,
          applicationId: appId ? appId : null,
        };
      });
      return {
        ...item,
        applicationId: appIds,
      };
    });

    if (detail) {
      const rule = { ...detail, ...values };
      rule.exceptTuple = JSON.stringify(tuples);
      updateForwardRule(rule).then(({ success }) => {
        if (success) {
          history.goBack();
        }
      });
    } else {
      createForwardRule({ ...values, exceptTuple: JSON.stringify(tuples) }).then(({ success }) => {
        if (success) {
          history.goBack();
        }
      });
    }
  };

  return (
    <Form form={form} onFinish={handleFinish} labelCol={{ span: 4 }} wrapperCol={{ span: 12 }}>
      <Form.Item label="名称" name="name">
        <Input />
      </Form.Item>
      <Form.Item label="默认动作" name="defaultAction">
        <Radio.Group
          options={Object.keys(EForwardRuleAction).map((item) => {
            return {
              label: item,
              value: EForwardRuleAction[item],
            };
          })}
        />
      </Form.Item>
      <Col span={12} offset={4}>
        <Divider style={{ minWidth: 'initial', margin: '16px auto' }} orientation="center">
          额外配置
        </Divider>
      </Col>
      <Form.Item label="BPF过滤条件" name="exceptBpf">
        <Input.TextArea />
      </Form.Item>

      <Form.Item label="流过滤条件" extra={exceptTupleExtra}>
        <Form.List name="exceptTuple">
          {(fields, { add, remove }) => {
            return (
              <Card bordered size="small">
                <Row gutter={4}>
                  <Col span={1} />
                  <Col span={3}>源IP</Col>
                  <Col span={2}>源端口</Col>
                  <Col span={3}>目的IP</Col>
                  <Col span={2}>目的端口</Col>
                  <Col span={3}>协议号</Col>
                  <Col span={3}>VLANID</Col>
                  <Col span={6}>应用</Col>
                  <Col span={1} />
                </Row>
                {fields.map(({ key, name, ...restField }) => {
                  return (
                    <Row key={key} gutter={4}>
                      <Col span={1}>
                        <Form.Item>{key + 1}</Form.Item>
                      </Col>
                      <Col span={3}>
                        <Form.Item
                          {...restField}
                          name={[name, 'sourceIp']}
                          rules={[
                            {
                              required: false,
                              message: '源IP不能为空',
                            },
                            { validator: checkSourceOrDestIp },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Col>
                      <Col span={2}>
                        <Form.Item
                          {...restField}
                          name={[name, 'sourcePort']}
                          rules={[
                            { required: false, message: '源端口不能为空' },
                            { validator: checkPort },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Col>
                      <Col span={3}>
                        <Form.Item
                          {...restField}
                          name={[name, 'destIp']}
                          rules={[
                            {
                              required: false,
                              message: '目的IP不能为空',
                            },
                            { validator: checkSourceOrDestIp },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Col>
                      <Col span={2}>
                        <Form.Item
                          {...restField}
                          name={[name, 'destPort']}
                          rules={[
                            { required: false, message: '目的端口不能为空' },
                            { validator: checkPort },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Col>
                      <Col span={3}>
                        <Form.Item {...restField} name={[name, 'protocol']}>
                          <Select
                            options={FILTER_PROTOCOL_TYPE_LIST}
                            fieldNames={{ label: 'label', value: 'key' }}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={3}>
                        <Form.Item
                          {...restField}
                          name={[name, 'vlanId']}
                          rules={[
                            {
                              required: false,
                              validator: checkVlan,
                            },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      </Col>
                      <Col span={6}>
                        <Form.Item {...restField} name={[name, 'applicationId']}>
                          <TreeSelect
                            treeData={appTree}
                            treeCheckable={true}
                            showSearch
                            maxTagCount={1}
                            treeNodeFilterProp={'title'}
                            style={{ width: '100%' }}
                            dropdownStyle={{ maxHeight: 400, overflow: 'auto', minWidth: 400 }}
                            showCheckedStrategy={TreeSelect.SHOW_PARENT}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={1}>
                        <Form.Item>
                          <MinusCircleOutlined onClick={() => remove(name)} />
                        </Form.Item>
                      </Col>
                    </Row>
                  );
                })}
                <Form.Item
                  style={{ marginBottom: 0, textAlign: 'center' }}
                  wrapperCol={{ span: 4, offset: 10 }}
                >
                  {fields.length < MAX_TUPLE_COUNT ? (
                    <Button type="primary" size="small" onClick={() => add()} block>
                      添加
                    </Button>
                  ) : (
                    <Button type="primary" size="small" disabled>
                      最多可配置{MAX_TUPLE_COUNT}个
                    </Button>
                  )}
                </Form.Item>
              </Card>
            );
          }}
        </Form.List>
      </Form.Item>
      <Form.Item
        label="备注"
        name="description"
        rules={[
          {
            required: false,
            message: '请填写备注',
          },
          {
            max: 255,
            message: '最多可输入255个字符',
          },
        ]}
      >
        <Input.TextArea rows={4} />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit">
            保存
          </Button>
          <Button
            style={{ marginLeft: 8 }}
            onClick={() => {
              history.goBack();
            }}
          >
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default RuleForm;
