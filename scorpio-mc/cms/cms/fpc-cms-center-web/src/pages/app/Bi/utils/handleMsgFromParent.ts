import { history } from 'umi';

export enum EMsgType {
  'URL_TO_URL' = 'url_to_url',
  'URL_TO_ID' = 'url_to_id',
}

export function handleMsgFromparents(ev: MessageEvent<{ type: EMsgType; param: any }>) {
  const { type, param } = ev.data || {};
  if (type === EMsgType.URL_TO_URL) {
    const { url, embedUrl } = param;
    history.push(`${url}?embedUrl=${embedUrl}`);
  } else if (type === EMsgType.URL_TO_ID) {
    const { url, id } = param;
    history.push(`${url}?id=${id}`);
  }
}
