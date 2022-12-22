import type { ConnectState } from '@/models/connect';
import type { IMitreAttack, IRuleClasstype } from '@/pages/app/security/typings';
import { CaretRightOutlined } from '@ant-design/icons';
import {
  Button,
  Checkbox,
  Collapse,
  Form,
  Input,
  InputNumber,
  message,
  Select,
  Space,
  Tabs,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { history, useDispatch, useSelector } from 'umi';
import {
  RuleDirectionOptions,
  RuleProtocolOptions,
  RuleSignatureSeverityOptions,
  RuleTargetOptions,
} from '../../common';
import { createSuricataRule, updateSuricataRule } from '../../service';
import type { ISuricataRule } from '../../typings';
import { ERuleSource, ERuleState } from '../../typings';
import styles from './index.less';

enum ERuleCreateType {
  Field = 'field',
  RuleContent = 'content',
}

/** 规则字段描述信息 */
const helpMessage = {
  IP: (
    <Collapse
      bordered={false}
      expandIcon={({ isActive }) => <CaretRightOutlined rotate={isActive ? 90 : 0} />}
    >
      <Collapse.Panel header="IP相关说明" key="IP" className={styles.panel}>
        <section>
          <ol>
            <li>any: 代表任意的IP地址</li>
            <li>10.0.0.0/24、10.0.0.1: 具体的IP地址</li>
            <li>[10.0.0.1, 10,0,0.2, !10.0.0.3]: IP为10.0.0.1或者10.0.0.2, 但不是10.0.0.3</li>
            <li>!1.1.1.1: 所有IP除了1.1.1.1</li>
            <li>![1.1.1.1, 2.2.2.2] 所有IP, 除了1.1.1.1和2.2.2.2</li>
            <li>[10.0.0.0/24, !10.0.0.5] 表示10.0.0.0/24 但不包括10.0.0.5</li>
          </ol>
        </section>
      </Collapse.Panel>
    </Collapse>
  ),
  port: (
    <Collapse
      bordered={false}
      expandIcon={({ isActive }) => <CaretRightOutlined rotate={isActive ? 90 : 0} />}
    >
      <Collapse.Panel header="端口说明" key="port" className={styles.panel}>
        <section>
          <ol>
            <li>[80,81,82]: 端口为80,81,82</li>
            <li>[80: 82]: 端口范围从80到82</li>
            <li>[1024: ]: 从1024开始到最大的端口号</li>
            <li>!80: 所有端口,除了80</li>
            <li>[1:80, ![2,4]]: 端口范围1到80, 除了端口2和4</li>
          </ol>
        </section>
      </Collapse.Panel>
    </Collapse>
  ),
  msg: <>规则中的msg关键字的描述信息,可用于告警中的提示</>,
  content: (
    <>
      匹配数据包的内容，可以配置ASCII码从0-255的字符，可打印字符比如a-z可以直接写，某些特殊字符需要使用16进制表示，
      可以使用关键字修饰例如nocase，depth等等 详细信息查看
      <a
        target="_blank"
        href="https://suricata.readthedocs.io/en/suricata-6.0.5/rules/payload-keywords.html?highlight=content#nocase"
      >
        Payload关键字
      </a>
      ，兼容suricata语法
    </>
  ),
  SID: <>每个规则的id,需要填写一个数字,不能重复</>,
  priortiy: <>范围1-255,数字越小，优先级越高，多条规则同时匹配时，优先应用priority字段小的规则</>,
  CVE: (
    <>
      表示规则引用CVE数据库的的相关信息，具体标号可以去
      <a target="_blank" href="http://cve.mitre.org/find/search_tips.html">
        CVE
      </a>
      查看
    </>
  ),
  CNNVD: (
    <>
      同CVE,
      <a target="_blank" href="http://www.cnnvd.org.cn/">
        具体地址
      </a>
    </>
  ),
  threshold: (
    <>
      阈值关键字可用于控制规则的警报频率。它具有3种模式：阈值，限制和两者。 语法type xxx, track xxx,
      count 12, seconds 60. 其中type的值可以为 threshold,limit,both track可填 by_src, by_dst,
      by_rule, by_both，兼容suricata语法
    </>
  ),
  rule: (
    <>
      请填写完整的规则，具体请查看
      <a target="_blank" href="https://suricata.readthedocs.io/en/suricata-6.0.5/rules/intro.html">
        规则格式参考
      </a>
    </>
  ),
};

interface Props {
  detail?: ISuricataRule;
}

const RuleForm = (props: Props) => {
  const { detail } = props;
  const [form] = Form.useForm();

  const [currentMitreAttackId, setCurrentMitreAttackId] = useState<string>();
  const [ruleText, setRuleText] = useState('');
  const [ruleCreateType, setRuleCreateType] = useState<ERuleCreateType>(ERuleCreateType.Field);

  const dispatch = useDispatch<Dispatch>();
  const mitreAttacks = useSelector<ConnectState, IMitreAttack[]>(
    (state) => state.suricataModel.mitreAttackList,
  );

  const ruleClassTypes = useSelector<ConnectState, IRuleClasstype[]>(
    (state) => state.suricataModel.classtypes,
  );

  useEffect(() => {
    if (detail) {
      form.setFieldsValue({
        ...detail,
        state: detail.state === '1',
      });
      setCurrentMitreAttackId(detail.mitreTacticId);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail]);

  useEffect(() => {
    if (!mitreAttacks.length) {
      dispatch({
        type: 'suricataModel/querySuricataMitreAttack',
      });
    }
    if (!ruleClassTypes.length) {
      dispatch({
        type: 'suricataModel/querySuricataRuleClasstype',
      });
    }
    // 初始化获取数据
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const attackTree = useMemo(() => {
    const tmp = mitreAttacks
      .filter((item) => !item.parentId)
      .map((item) => {
        return { label: item.name, value: item.id, children: [] as any[] };
      });

    mitreAttacks.forEach((item) => {
      if (item.parentId) {
        const parentIndex = tmp.findIndex((att) => att.value === item.parentId);
        if (parentIndex !== -1) {
          tmp[parentIndex].children.push({ label: item.name, value: item.id });
        }
      }
    });

    return tmp;
  }, [mitreAttacks]);

  // 创建规则时，表单填充默认值
  useEffect(() => {
    if (!detail && attackTree.length > 0 && ruleClassTypes.length > 0) {
      setCurrentMitreAttackId('0');
      form.setFieldsValue({
        classtypeId: '0',
        mitreTacticId: '0',
        signatureSeverity: '2',
      });
    }
  }, [attackTree, detail, form, ruleClassTypes]);

  const ruleClassTypeOptions = useMemo(() => {
    return ruleClassTypes.map((item) => {
      return {
        label: item.name,
        value: item.id,
      };
    });
  }, [ruleClassTypes]);

  const handleMitreAttackChange = (value: string) => {
    setCurrentMitreAttackId(value);
    form.setFieldsValue({
      mitreTechniqueId: undefined,
    });
  };

  const handleFinish = ({ rule, ...restValues }: any) => {
    if (detail) {
      updateSuricataRule({
        ...restValues,
        state: restValues.state ? ERuleState.启用 : ERuleState.停用,
      }).then((res) => {
        if (res.success) {
          message.success('修改成功');
          history.goBack();
        } else {
          message.error('编辑失败');
        }
      });
    } else {
      const values: any = ruleCreateType === ERuleCreateType.Field ? { ...restValues } : { rule };
      createSuricataRule({
        ...values,
        state: restValues.state ? ERuleState.启用 : ERuleState.停用,
        source: ERuleSource.自定义,
      }).then((res) => {
        if (res.success) {
          message.success('创建成功');
          history.goBack();
        } else {
          message.error('创建失败');
        }
      });
    }
  };

  const fieldRequire = ruleCreateType === ERuleCreateType.Field;

  const fieldForm = (
    <>
      <Form.Item name="protocol" rules={[{ required: fieldRequire }]} label="协议">
        <Select options={RuleProtocolOptions} />
      </Form.Item>
      <Form.Item
        name="srcIp"
        rules={[{ required: fieldRequire }]}
        label="源IP"
        help={helpMessage.IP}
      >
        <Input />
      </Form.Item>
      <Form.Item
        name="srcPort"
        rules={[{ required: fieldRequire }]}
        label="源端口"
        help={helpMessage.port}
      >
        <Input />
      </Form.Item>
      <Form.Item name="destIp" rules={[{ required: fieldRequire }]} label="目的IP">
        <Input />
      </Form.Item>
      <Form.Item name="destPort" rules={[{ required: fieldRequire }]} label="目的端口">
        <Input />
      </Form.Item>
      <Form.Item name="direction" rules={[{ required: fieldRequire }]} label="方向">
        <Select options={RuleDirectionOptions} />
      </Form.Item>
      <Form.Item
        name="msg"
        rules={[{ required: fieldRequire }]}
        label="规则描述"
        help={helpMessage.msg}
      >
        <Input />
      </Form.Item>
      <Form.Item
        name="content"
        // rules={[{ required: fieldRequire }]}
        label="规则正文"
        help={helpMessage.content}
      >
        <Input />
      </Form.Item>
      <Form.Item name="action" rules={[{ required: fieldRequire }]} label="动作">
        <Select options={[{ label: '告警', value: 'alert' }]} />
      </Form.Item>
      <Form.Item
        name="sid"
        rules={[{ required: fieldRequire, min: 1, type: 'number' }]}
        label="规则ID"
        help={helpMessage.SID}
      >
        <InputNumber disabled={detail !== undefined} />
      </Form.Item>
      <Form.Item name="priority" label="优先级" help={helpMessage.priortiy}>
        <InputNumber min={0} max={255} />
      </Form.Item>
      <Form.Item name="classtypeId" label="规则分类">
        <Select options={ruleClassTypeOptions} />
      </Form.Item>
      <Form.Item label="战术分类-技术分类" wrapperCol={{ span: 12 }}>
        <Input.Group compact>
          <Form.Item
            name="mitreTacticId"
            // rules={[{ required: fieldRequire, message: '请填入战术分类' }]}
            noStyle
          >
            <Select
              options={attackTree}
              onChange={handleMitreAttackChange}
              style={{ width: '50%' }}
            />
          </Form.Item>
          <Form.Item
            name="mitreTechniqueId"
            // rules={[
            //   {
            //     required: fieldRequire && currentMitreAttackId !== '0',
            //     message: '请填入技术分类',
            //   },
            // ]}
            noStyle
          >
            <Select
              disabled={currentMitreAttackId === '0'}
              style={{ width: '50%' }}
              options={attackTree.find((item) => item.value === currentMitreAttackId)?.children}
            />
          </Form.Item>
        </Input.Group>
      </Form.Item>
      <Form.Item name="cve" label="CVE编号" help={helpMessage.CVE}>
        <Input />
      </Form.Item>
      <Form.Item name="cnnvd" label="CNNVD编号" help={helpMessage.CNNVD}>
        <Input />
      </Form.Item>
      <Form.Item name="signatureSeverity" label="严重级别">
        <Select options={RuleSignatureSeverityOptions} />
      </Form.Item>
      <Form.Item name="target" label="受害方" rules={[{ required: true }]}>
        <Select options={RuleTargetOptions} allowClear />
      </Form.Item>
      <Form.Item
        name="threshold"
        label="告警频率"
        help={helpMessage.threshold}
        rules={[
          {
            message: '内容必须包含threshold | track | detection_filter 之一',
            validator: (_, value: string) => {
              if (value && value.search(/threshold|track|detection_filter/) === -1) {
                return Promise.reject();
              }
              return Promise.resolve();
            },
          },
        ]}
      >
        <Input />
      </Form.Item>
    </>
  );

  return (
    <Form form={form} wrapperCol={{ span: 12 }} labelCol={{ span: 4 }} onFinish={handleFinish}>
      {/* 创建规则时渲染Tab */}
      {detail === undefined && (
        <Tabs
          onChange={(key) => setRuleCreateType(key as ERuleCreateType)}
          activeKey={ruleCreateType}
          centered
          defaultActiveKey={ERuleCreateType.Field}
        >
          <Tabs.TabPane tab="按字段配置" key={ERuleCreateType.Field}>
            {/* 仅创建时渲染 */}
          </Tabs.TabPane>
          <Tabs.TabPane tab="填入完整规则内容" key={ERuleCreateType.RuleContent} />
        </Tabs>
      )}
      {ruleCreateType === ERuleCreateType.Field && fieldForm}
      {detail === undefined && ruleCreateType === ERuleCreateType.RuleContent && (
        <Form.Item name="rule" label="规则文本" help={helpMessage.rule}>
          <Input.TextArea
            autoSize
            value={ruleText}
            onChange={(e) => setRuleText(e.target.value)}
            showCount
            allowClear
          />
        </Form.Item>
      )}
      <Form.Item name="state" label="启用" valuePropName="checked">
        <Checkbox />
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit">
            保存
          </Button>
          <Button
            onClick={() => {
              // history.goBack();
              history.push('/configuration/safety-analysis/suricata/rule');
            }}
          >
            返回
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};

export default RuleForm;
