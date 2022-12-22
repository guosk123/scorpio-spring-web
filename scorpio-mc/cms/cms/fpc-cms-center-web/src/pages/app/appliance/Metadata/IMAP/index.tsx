import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';

interface Props {
  paneTitle?: string;
}

const MetadataIMAP = (props: Props) => {
  const { paneTitle } = props;
  return <Mail entry={paneTitle || ''} protocol={EMetadataProtocol.IMAP} />;
};

export default MetadataIMAP;
