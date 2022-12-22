import { tableColumns } from '@/pages/app/appliance/Metadata/HTTP';
import type { IMetadataHttp } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadataTable';

const HTTP = () => {
  return (
    <DeferedMetadataTable<IMetadataHttp>
      tableColumns={tableColumns()}
      protocol={EMetadataProtocol.HTTP}
    />
  );
};

export default HTTP;
