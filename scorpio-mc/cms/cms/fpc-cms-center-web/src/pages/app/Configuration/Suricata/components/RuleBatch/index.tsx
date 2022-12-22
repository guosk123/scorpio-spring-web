import InputAndSelect from '@/components/InputAndSelect';
import useQuery from '@/hooks/useQuery';
import type { ConnectState } from '@/models/connect';
import type { IMitreAttack, IRuleClasstype } from '@/pages/app/security/typings';
import { useSafeState } from 'ahooks';
import { Button, Checkbox, Form, Input, message, Popconfirm, Select, Space } from 'antd';
import { useForm } from 'antd/es/form/Form';
import { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch, useSelector } from 'umi';
import { batchChangeSuricataRule, batchDeleteSuricataRule } from '../../service';
import { ERuleSource } from '../../typings';

interface Props {
  search: Record<string, any>;
  onFinish: () => void;
  sources: Record<string, string>;
}

const RuleBatch = ({ search, onFinish, sources }: Props) => {
  const [form] = useForm();

  const [currentMitreAttackId, setCurrentMitreAttackId] = useState<string>();
  const [loading, setLoading] = useSafeState(false);

  const dispatch = useDispatch<Dispatch>();
  const mitreAttacks = useSelector<ConnectState, IMitreAttack[]>(
    (state) => state.suricataModel.mitreAttackList,
  );

  const ruleClassTypes = useSelector<ConnectState, IRuleClasstype[]>(
    (state) => state.suricataModel.classtypes,
  );

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

  const ruleClassTypeOptions = useMemo(() => {
    return ruleClassTypes.map((item) => {
      return {
        label: item.name,
        value: item.id,
      };
    });
  }, [ruleClassTypes]);

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

  const handleMitreAttackChange = (value: string) => {
    setCurrentMitreAttackId(value);
    form.setFieldsValue({
      mitreTechniqueId: undefined,
    });
  };

  const handleDeleteBatch = async () => {
    return batchDeleteSuricataRule(search);
  };

  const [deleteLoading, handleDelte] = useQuery(handleDeleteBatch);

  const handleRuleBatchChange = (values: Record<string, any>) => {
    const keys = Object.keys(values).filter((k) => values[k] !== undefined);

    const changed = keys.reduce((prev, curr) => {
      let value = values[curr];
      if (curr === 'state') {
        value = value === true ? '1' : '0';
      }
      return {
        ...prev,
        [curr]: value,
      };
    }, {});
    setLoading(true);
    batchChangeSuricataRule(search, changed).then((res) => {
      setLoading(false);
      const { success } = res;
      if (!success) {
        message.error('修改失败');
      }
      if (success) {
        onFinish();
      }
    });
  };

  return (
    <Form
      form={form}
      wrapperCol={{ span: 12 }}
      labelCol={{ span: 4 }}
      onFinish={handleRuleBatchChange}
      initialValues={{
        state: true,
      }}
    >
      {!search.sids && (
        <Form.Item name="state" label="启用状态" valuePropName="checked">
          <Checkbox />
        </Form.Item>
      )}
      <Form.Item name="source" label="来源">
        <InputAndSelect
          allowClear
          options={Object.keys(sources)
            .map((key) => {
              return {
                label: sources[key],
                value: key,
              };
            })
            .filter((item) => item.value !== ERuleSource.系统内置)}
        />
      </Form.Item>
      <Form.Item name="classtypeId" label="规则分类">
        <Select options={ruleClassTypeOptions} allowClear />
      </Form.Item>
      <Form.Item label="战术分类-技术分类" wrapperCol={{ span: 12 }}>
        <Input.Group compact>
          <Form.Item name="mitreTacticId" noStyle>
            <Select
              options={attackTree}
              onChange={handleMitreAttackChange}
              style={{ width: '50%' }}
              allowClear
            />
          </Form.Item>
          <Form.Item name="mitreTechniqueId" noStyle>
            <Select
              allowClear
              disabled={currentMitreAttackId === '0'}
              style={{ width: '50%' }}
              options={attackTree.find((item) => item.value === currentMitreAttackId)?.children}
            />
          </Form.Item>
        </Input.Group>
      </Form.Item>
      <Form.Item wrapperCol={{ offset: 4 }}>
        <Space>
          <Button type="primary" htmlType="submit" loading={loading}>
            修改
          </Button>
          {!search.sids && (
            <Popconfirm
              title="是否确认删除所有条件命中规则"
              onConfirm={handleDelte}
              okText="是"
              cancelText="取消"
            >
              <Button danger loading={deleteLoading}>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      </Form.Item>
    </Form>
  );
};

export default RuleBatch;
