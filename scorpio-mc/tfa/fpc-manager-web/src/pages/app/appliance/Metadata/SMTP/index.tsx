import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const MetadataSMTP = () => {
  const { pathname } = history.location;
  return <Mail entry={getEntryTag(pathname)} protocol={EMetadataProtocol.SMTP} />;
};

export default MetadataSMTP;
