import { Tooltip, Typography } from 'antd';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataTelnet } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const tableColumns: IColumnProps<IMetadataTelnet>[] = [
  {
    title: '登录用户',
    dataIndex: 'username',
    searchable: true,
  },
  // {
  //   title: '登录密码',
  //   dataIndex: 'password',
  //   searchable: true,
  // },
  {
    title: '操作命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '操作结果',
    dataIndex: 'reply',
    searchable: true,
    render: (text) => (
      <Tooltip
        placement="bottomLeft"
        title={text}
        zIndex={800}
        overlayInnerStyle={{ maxWidth: 300, overflow: 'auto', maxHeight: 300 }}
      >
        <Typography.Text
          style={{ width: 160, cursor: 'pointer' }}
          ellipsis={{
            tooltip: false,
          }}
        >
          {text}
        </Typography.Text>
      </Tooltip>
    ),
  },
];

const MetadataTelnet = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.TELNET}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataTelnet;
