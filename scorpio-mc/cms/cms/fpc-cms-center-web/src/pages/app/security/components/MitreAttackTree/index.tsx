import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { Button, Card, Space, Tree } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { useDispatch, useSelector } from 'umi';
import type { IMitreAttack } from '../../typings';

interface Props {
  onChange?: (checked: string[]) => void;
  style?: React.CSSProperties;
  className?: string;
}

const MitreAttackTree = (props: Props) => {
  const { onChange, style, className } = props;

  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);
  const dispatch = useDispatch<Dispatch>();
  const { originStartTime: startTime, originEndTime: endTime } = useSelector<
    ConnectState,
    Required<IGlobalTime>
  >((state) => state.appModel.globalSelectedTime);

  const attacks = useSelector<ConnectState, IMitreAttack[]>(
    (state) => state.suricataModel.mitreAttackList,
  );

  const loading = useSelector<ConnectState, boolean>(
    (state) => !!state.loading.effects['suricataModel/querySuricataMitreAttack'],
  );

  useEffect(() => {
    dispatch({
      type: 'suricataModel/querySuricataMitreAttack',
      payload: { startTime, endTime },
    });

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startTime, endTime]);

  const attackTree = useMemo(() => {
    const tmp = attacks
      .filter((item) => !item.parentId)
      .map((item) => {
        return {
          title: `${item.name}(${item.id})(${item.alertSize})`,
          key: item.id,
          children: [] as any[],
        };
      });

    attacks.forEach((item) => {
      if (item.parentId) {
        tmp.forEach((att, index) => {
          if (item.parentId.split(',').includes(att.key)) {
            tmp[index].children.push({
              title: `${item.name}(${item.id})(${item.alertSize})`,
              key: `${att.key}-${item.id}`,
            });
          }
        });
      }
    });

    // 找到其他分类，并将其放在树的最底下
    const otherClassIndex = tmp.findIndex((item) => item.key === '0');
    if (otherClassIndex > -1) {
      const otherClass = tmp.splice(otherClassIndex, 1)[0];
      tmp.push(otherClass);
    }

    setCheckedKeys(tmp.map((item) => item.key));

    return tmp;
  }, [attacks]);

  const handleChecked = (checked: any) => {
    setCheckedKeys(checked as string[]);
  };

  useEffect(() => {
    if (!loading) {
      onChange?.(checkedKeys);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checkedKeys, loading]);

  return (
    <Card
      title="ATT&CK分类"
      loading={loading}
      size="small"
      className={className}
      style={{ ...style }}
      bodyStyle={{ paddingLeft: 0 }}
      extra={
        <Space>
          <Button
            size="small"
            onClick={() => {
              setCheckedKeys(attackTree.map((item) => item.key));
            }}
          >
            全选
          </Button>
          <Button
            size="small"
            onClick={() => {
              setCheckedKeys([]);
            }}
          >
            取消
          </Button>
        </Space>
      }
    >
      <Tree treeData={attackTree} checkable onCheck={handleChecked} checkedKeys={checkedKeys} />
    </Card>
  );
};

export default MitreAttackTree;
