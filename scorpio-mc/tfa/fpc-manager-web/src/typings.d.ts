declare module 'slash2';
declare module '*.css';
declare module '*.less';
declare module '*.scss';
declare module '*.sass';
declare module '*.svg';
declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.bmp';
declare module '*.tiff';
declare module 'omit.js';
declare module 'dva-model-extend';
declare module 'react-split-pane/lib/Pane';
declare module 'react-drag-listview';
declare module 'save-svg-as-png';

// google analytics interface
interface GAFieldsObject {
  eventCategory: string;
  eventAction: string;
  eventLabel?: string;
  eventValue?: number;
  nonInteraction?: boolean;
}
interface Window {
  reloadAuthorized: () => void;
  cancelRequest: Map;
  /** 服务器时间 */
  systemTime: string | undefined;
  /**
   * 客户端时间与服务器时间的差值（秒）
   * @description 客户端时间 - 服务器时间
   */
  clientTimeDiffSystemTimeSeconds: number;
}

declare const REACT_APP_ENV: 'test' | 'dev' | 'pre' | false;

declare const appConfig: {
  node_info: Record<string, any>;
  [property: string]: any;
};
