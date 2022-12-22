import { tableColumns } from '@/pages/app/appliance/Metadata/FTP';
import type { IMetadataFtp } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const FTP = () => {
  return (
    <DeferedMetadataTable<IMetadataFtp>
      tableColumns={tableColumns}
      protocol={EMetadataProtocol.FTP}
    />
  );
};

export default FTP;
