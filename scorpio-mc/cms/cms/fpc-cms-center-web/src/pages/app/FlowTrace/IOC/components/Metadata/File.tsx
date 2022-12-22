import { tableColumns } from '@/pages/app/appliance/Metadata/File';
import type { IMetadataFile } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataProtocol } from '@/pages/app/appliance/Metadata/typings';
import DeferedMetadataTable from '../DeferedMetadata';

const File = () => {
  return (
    <DeferedMetadataTable<IMetadataFile>
      tableColumns={tableColumns}
      protocol={EMetadataProtocol.FILE}
      timeField="timestamp"
    />
  );
};

export default File;
