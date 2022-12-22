import useFlowRecordColumns from '@/pages/app/appliance/FlowRecords/Record/hooks/useFlowRecordColumns';
import useColumnForMetadata from '@/pages/app/appliance/Metadata/hooks/useColumnForMetadata';
import { METADATA_FIELD_MAP } from '../../../../dict';

function useLogTypeField<T>({ index }: { index: string }) {
  const flowLogColumns = useFlowRecordColumns({} as any);
  const { fullColumns: metaDataColumns } = useColumnForMetadata({
    protocol: index || '',
    tableColumns: METADATA_FIELD_MAP[index] || [],
  });

  if (index === 'flowlog') {
    return flowLogColumns?.map((c) => ({
      ...c,
      disabled: false,
    })) as T;
  }

  if (index === 'icmp') {
    return metaDataColumns.map((c) => {
      if (c?.dataIndex === 'requestDataLen' || c?.dataIndex === 'responseDataLen') {
        return {
          ...c,
          searchable: true,
        };
      }
      return c;
    });
  }
  return metaDataColumns as T;
}

export default useLogTypeField;
