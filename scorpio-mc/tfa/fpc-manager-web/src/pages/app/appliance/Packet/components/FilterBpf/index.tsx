import { bpfValid } from '@/utils/utils';
import { InfoCircleOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Form, Input, Tooltip } from 'antd';
import lodash from 'lodash';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { IPacketConnectState } from '../../typings';
import styles from './index.less';
import DslExamples from '../DslExamples';

interface IFilterBpfProps {
  dispatch: Dispatch;
  bpfData: string;
  queryLoading: boolean | undefined;
  defaultBpf: string;
  onChange: (bpf: string) => void;
}
const FilterBpf = ({ dispatch, bpfData, queryLoading, defaultBpf, onChange }: IFilterBpfProps) => {
  useEffect(() => {
    return () => {
      dispatch({
        type: 'packetModel/updateBpf',
        payload: {
          bpfData: '',
        },
      });
    };
  }, [dispatch]);

  /** 校验BPF语法是否合法 */
  const debouncedBpfValid = lodash.debounce(bpfValid, 300);

  const handleFinish = (fieldsValue: Record<string, any>) => {
    onChange(fieldsValue.bpf);
    dispatch({
      type: 'packetModel/updateBpf',
      payload: {
        bpfData: fieldsValue.bpf,
      },
    });
  };

  return (
    <Form
      onFinish={handleFinish}
      scrollToFirstError
      initialValues={{
        bpf: defaultBpf || bpfData,
      }}
    >
      <div className={styles['bpf-wrap']}>
        <Form.Item
          className={styles['bpf-input']}
          name="bpf"
          rules={[
            {
              validator: debouncedBpfValid,
            },
          ]}
        >
          <Input
            onPressEnter={(e) => e.preventDefault()}
            placeholder="请输入BPF语句"
            prefix={
              <Tooltip title="支持标准的BPF语法">
                <InfoCircleOutlined />
              </Tooltip>
            }
          />
        </Form.Item>
        <DslExamples />
        <Button
          loading={queryLoading}
          icon={<SearchOutlined />}
          type="primary"
          htmlType="submit"
          className={styles['bpf-submit']}
        >
          查询
        </Button>
      </div>
    </Form>
  );
};

export default connect(
  ({ packetModel: { bpfData }, loading: { effects } }: IPacketConnectState) => ({
    queryLoading:
      effects['packetModel/queryPacketList'] || effects['packetModel/queryPacketRefine'],
    bpfData,
  }),
)(FilterBpf);
