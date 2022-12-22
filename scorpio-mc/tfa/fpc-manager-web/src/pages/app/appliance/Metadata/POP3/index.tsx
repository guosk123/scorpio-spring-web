import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const MetadataPOP3 = () => {
  const { pathname } = history.location;
  return <Mail entry={getEntryTag(pathname)} protocol={EMetadataProtocol.POP3} />;
};

export default MetadataPOP3;
