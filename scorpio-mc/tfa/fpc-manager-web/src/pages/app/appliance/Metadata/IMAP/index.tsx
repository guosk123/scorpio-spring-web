import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const MetadataIMAP = () => {
  const { pathname } = history.location;
  return <Mail entry = {getEntryTag(pathname)} protocol={EMetadataProtocol.IMAP} />;
};

export default MetadataIMAP;
