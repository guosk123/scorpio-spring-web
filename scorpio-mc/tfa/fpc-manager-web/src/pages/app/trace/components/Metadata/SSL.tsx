import { EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '@/pages/app/appliance/Metadata/components/Template';
import { ellipsisText } from '@/pages/app/appliance/Metadata/Mail';
import { tableColumns } from '@/pages/app/appliance/Metadata/SSL';
import type { IMetadataSsl } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadataTable';

const finalCols: IColumnProps<IMetadataSsl>[] = [
  ...tableColumns,
  {
    dataIndex: 'serverCertsSha1',
    title: '证书指纹',
    searchable: true,
    show: false,
    fieldType: EFieldType.ARRAY,
    render: (text) => ellipsisText(text.join(',')),
  },
];

const SSL = () => {
  return (
    <DeferedMetadataTable<IMetadataSsl> tableColumns={finalCols} protocol={EMetadataProtocol.SSL} />
  );
};

export default SSL;
