import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';

export const tableColumns = [
  {
    title: '详细信息',
    dataIndex: 'result',
  },
];

const MetadataIcmp = ({location}: {location: {pathname: string}}) => {
  return <Template entry = {getEntryTag(location.pathname)} protocol={EMetadataProtocol.ICMP} tableColumns={tableColumns} />;
};

export default MetadataIcmp;
