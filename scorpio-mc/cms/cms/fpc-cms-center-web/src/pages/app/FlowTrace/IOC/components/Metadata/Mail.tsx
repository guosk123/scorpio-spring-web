import { tableColumns } from '@/pages/app/appliance/Metadata/Mail';
import type { IMetadataMail } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const Mail = () => {
  return (
    <DeferedMetadataTable<IMetadataMail>
      tableColumns={tableColumns}
      protocol={EMetadataProtocol.MAIL}
    />
  );
};

export default Mail;
