import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';

export const tableColumns = [
  {
    title: '用户名',
    dataIndex: 'username',
    searchable: true,
  },
  {
    title: '数据库名称',
    dataIndex: 'databaseName',
    searchable: true,
  },
  {
    title: 'sql命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '错误',
    dataIndex: 'error',
    searchable: true,
  },
  {
    title: '响应时间(ms)',
    dataIndex: 'delaytime',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
];

interface Props {
  paneTitle?: string;
}

const MetadataMysql = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template entry={paneTitle} protocol={EMetadataProtocol.MYSQL} tableColumns={tableColumns} />
  );
};

export default MetadataMysql;
