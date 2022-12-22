import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';

export const tableColumns = [
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

const MetadataTDS = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template entry={paneTitle} protocol={EMetadataProtocol.TDS} tableColumns={tableColumns} />
  );
};

export default MetadataTDS;
