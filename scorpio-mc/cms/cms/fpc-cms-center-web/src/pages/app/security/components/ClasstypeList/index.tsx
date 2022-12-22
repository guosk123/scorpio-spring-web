import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import type { IRuleClasstype } from '@/pages/app/security/typings';
import { Button, Card, Checkbox, Col, Row, Space } from 'antd';
import type { CheckboxValueType } from 'antd/lib/checkbox/Group';
import { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch, useSelector } from 'umi';
interface Props {
  onChange: (values: string[]) => void;
  style?: React.CSSProperties;
  className?: string;
}

const RuleClasstypeList = (props: Props) => {
  const { onChange, style, className } = props;

  const [ready, setReady] = useState(false);
  const dispatch = useDispatch<Dispatch>();
  const classtypes = useSelector<ConnectState, IRuleClasstype[]>(
    (state) => state.suricataModel.classtypes,
  );
  const { startTime, endTime } = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const [checkedValues, setCheckedValues] = useState<string[]>([]);

  const loading = useSelector<ConnectState, boolean>(
    (state) => state.loading.effects['suricataModel/querySuricataRuleClasstype'] || false,
  );

  useEffect(() => {
    dispatch({
      type: 'suricataModel/querySuricataRuleClasstype',
      payload: { startTime, endTime },
    }).then((res: any) => {
      setCheckedValues((res || { result: [] }).result.map((item: any) => item.id));
      setReady(true);
    });

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startTime, endTime]);

  const handleChange = (values: CheckboxValueType[]) => {
    setCheckedValues(values as string[]);
  };

  useEffect(() => {
    if (ready && !loading) {
      onChange(checkedValues);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checkedValues, loading, ready]);

  return (
    <Card
      size="small"
      style={style}
      loading={loading}
      className={className}
      extra={
        <Space>
          <Button
            size="small"
            onClick={() => {
              setCheckedValues(classtypes.map((item) => item.id));
            }}
          >
            全选
          </Button>
          <Button
            size="small"
            onClick={() => {
              setCheckedValues([]);
            }}
          >
            取消
          </Button>
        </Space>
      }
      title="规则分类"
      bodyStyle={{ paddingLeft: 24 }}
      headStyle={{ padding: 4, paddingLeft: '1em' }}
    >
      <Checkbox.Group onChange={handleChange} value={checkedValues}>
        <Row>
          {classtypes.map((classtype) => {
            return (
              <Col
                span={24}
                key={classtype.id}
                style={{ display: 'flex', justifyContent: 'space-between' }}
              >
                <Checkbox value={classtype.id}>
                  {classtype.name}({classtype.alertSize || 0})
                </Checkbox>
              </Col>
            );
          })}
        </Row>
      </Checkbox.Group>
    </Card>
  );
};

export default RuleClasstypeList;
