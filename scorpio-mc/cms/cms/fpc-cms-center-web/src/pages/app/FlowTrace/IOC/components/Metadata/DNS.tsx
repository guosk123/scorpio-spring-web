import { tableColumns } from '@/pages/app/appliance/Metadata/DNS';
import type { IMetadataDns } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const DNS = () => {
  return (
    <DeferedMetadataTable<IMetadataDns>
      tableColumns={tableColumns}
      protocol={EMetadataProtocol.DNS}
    />
  );
};

export default DNS;
