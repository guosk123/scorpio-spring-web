import FlowRecords from '@/pages/app/appliance/FlowRecords/Record';

export default function FlowRecord({ paneKey }: { paneKey?: string }) {
  return <FlowRecords tabName={paneKey?.split('^')[0]} />;
}
