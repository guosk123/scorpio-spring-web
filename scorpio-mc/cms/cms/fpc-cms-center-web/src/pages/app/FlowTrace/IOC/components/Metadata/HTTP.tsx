import { tableColumns } from '@/pages/app/appliance/Metadata/HTTP/Specifications';
import type { IMetadataHttp } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const HTTP = () => {
  return (
    <DeferedMetadataTable<IMetadataHttp>
      tableColumns={tableColumns()}
      protocol={EMetadataProtocol.HTTP}
    />
  );
};

export default HTTP;
