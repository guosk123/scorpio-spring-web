import type { IColumnProps } from '@/pages/app/appliance/Metadata/components/Template';
import { tableColumns } from '@/pages/app/appliance/Metadata/SSL';
import type { IMetadataSsl } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const finalCols: IColumnProps<IMetadataSsl>[] = [...tableColumns];

const SSL = () => {
  return (
    <DeferedMetadataTable<IMetadataSsl> tableColumns={finalCols} protocol={EMetadataProtocol.SSL} />
  );
};

export default SSL;
