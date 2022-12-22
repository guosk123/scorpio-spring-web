import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';
import type {
  IMetadataDb2,
} from '../typings';
import { history } from 'umi';
import { getEntryTag } from '../utils/entryTools';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';

const tableColumns: IColumnProps<IMetadataDb2>[] = [
  {
    title: 'DRDA用途',
    dataIndex: 'codePoint',
    searchable: true,
  },
  {
    title: 'DRDA数据',
    dataIndex: 'data',
    searchable: true,
    fieldType: EFieldType.Map,    
  },
];

const MetadataDb2 = () => {
  const { pathname } = history.location;
  return (
    <>
      <Template<IMetadataDb2>
        entry={getEntryTag(pathname)}
        protocol={EMetadataProtocol.DB2}
        tableColumns={tableColumns}
        isNewIpFieldType={true}
      />
    </>
  );
};

export default MetadataDb2;
