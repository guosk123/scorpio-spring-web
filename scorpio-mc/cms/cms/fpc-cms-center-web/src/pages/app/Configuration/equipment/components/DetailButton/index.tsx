import type { TableColumnProps } from 'antd';
import { Button, Drawer, Table } from 'antd';
import { Fragment, useMemo, useState } from 'react';

export default function DetailButton(props: any) {
  const { detail, col } = props;
  const data = useMemo(() => {
    if (!detail) {
      return [];
    }
    return Object.keys(detail)
      .filter((sub) => {
        return col.find((ele: any) => ele.key === sub)?.title;
      })
      .map((item) => ({
        key: col.find((ele: any) => ele.key === item)?.title,
        value: col.find((ele: any) => ele.key === item)?.render
          ? col.find((ele: any) => ele.key === item)?.render(detail[item])
          : detail[item],
      }));
  }, [col, detail]);
  const [visible, setVisible] = useState(false);
  const showDrawer = () => {
    setVisible(true);
  };
  const onClose = () => {
    setVisible(false);
  };
  const columns: TableColumnProps<any>[] = [
    {
      title: '属性',
      dataIndex: 'key',
      key: 'key',
      align: 'center',
      width: 100,
    },
    {
      title: '值',
      dataIndex: 'value',
      key: 'value',
      align: 'center',
      width: 100,
    },
  ];
  return (
    <Fragment>
      <Button type="link" size="small" onClick={showDrawer}>
        详情
      </Button>
      <Drawer title="详情" placement="right" onClose={onClose} visible={visible} width={400}>
        <Table size={'small'} bordered pagination={false} columns={columns} dataSource={data} />
      </Drawer>
    </Fragment>
  );
}
