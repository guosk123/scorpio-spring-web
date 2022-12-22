import { EMetadataProtocol } from '../typings';
import Mail from '../Mail';

interface Props {
  paneTitle?: string;
}

const MetadataPOP3 = (props: Props) => {
  const { paneTitle } = props;
  return <Mail entry={paneTitle || ''} protocol={EMetadataProtocol.POP3} />;
};

export default MetadataPOP3;
