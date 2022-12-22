import config from '@/common/applicationConfig';
import type { Reducer } from 'umi';
import type { DefaultSettings } from '../../../config/defaultSettings';
import defaultSettings from '../../../config/defaultSettings';
import type { Subscription } from './global';

export const SITE_THEME_KEY = 'theme';
/**
 * 主题
 */
export type TTheme = 'light' | 'dark';

export enum ETheme {
  'light' = 'light',
  'dark' = 'dark',
}

export interface SettingModelType {
  namespace: string;
  state: DefaultSettings;
  reducers: {
    changeSetting: Reducer<DefaultSettings>;
    changeTheme: Reducer<DefaultSettings>;
  };
  subscriptions: {
    setup: Subscription;
  };
}

/**
 * 更新皮肤
 * @param dark 是否暗黑模式
 * @param publicPath 皮肤的路径
 */
export function updateTheme(dark: boolean, publicPath = `${config.CONTEXT_PATH}/web-static/theme`) {
  const href = dark ? `${publicPath}/dark.css` : ``;
  const dom = document.getElementById('theme-style') as HTMLLinkElement;
  const hide: any = () => null;
  if (!href) {
    if (dom) {
      dom.remove();
      localStorage.removeItem(SITE_THEME_KEY);
    }
    return;
  }
  if (dom) {
    dom.onload = () => {
      window.setTimeout(() => {
        hide();
      });
    };
    dom.href = href;
  } else {
    const style = document.createElement('link');
    style.type = 'text/css';
    style.rel = 'stylesheet';
    style.id = 'theme-style';
    style.onload = () => {
      window.setTimeout(() => {
        hide();
      });
    };
    style.href = href;
    if (document.body.append) {
      document.body.append(style);
    } else {
      document.body.appendChild(style);
    }
  }

  localStorage.setItem(SITE_THEME_KEY, dark ? 'dark' : 'light');
}

const updateColorWeak: (colorWeak: boolean) => void = (colorWeak) => {
  const root = document.getElementById('root');
  if (root) {
    root.className = colorWeak ? 'colorWeak' : '';
  }
};

const SettingModel: SettingModelType = {
  namespace: 'settings',
  state: defaultSettings,
  reducers: {
    changeSetting(state = defaultSettings, { payload }) {
      const { theme, colorWeak, contentWidth } = payload;

      if (state.contentWidth !== contentWidth && window.dispatchEvent) {
        window.dispatchEvent(new Event('resize'));
      }
      updateColorWeak(!!colorWeak);
      updateTheme(theme);
      return {
        ...state,
        ...payload,
      };
    },
    changeTheme(state = defaultSettings, { payload }) {
      const { theme } = payload;

      let init = {
        navTheme: 'light',
        headerTheme: 'light',
      } as DefaultSettings;

      const isDark = theme === 'dark';

      if (isDark) {
        init = {
          navTheme: 'realDark',
          headerTheme: 'dark',
        } as DefaultSettings;
      }
      const body = document.getElementsByTagName('html')[0];
      body.className = theme;

      updateTheme(isDark);
      return {
        ...state,
        ...payload,
        ...init,
      };
    },
  },
  subscriptions: {
    setup({ dispatch }) {
      // 读取现有的theme
      const currentTheme = localStorage.getItem(SITE_THEME_KEY) || ETheme.light;
      dispatch({
        type: 'changeTheme',
        payload: {
          theme: currentTheme,
        },
      });
    },
  },
};
export default SettingModel;
