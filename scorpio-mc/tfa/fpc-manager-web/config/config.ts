// https://umijs.org/config/
import { defineConfig } from 'umi';
import defaultSettings from './defaultSettings';
import APPLICATION_CONFIG from '../src/common/applicationConfig';
import routes from './routes';

export default defineConfig({
  base: '/',
  publicPath: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/`,
  hash: true,
  antd: {},
  favicon: APPLICATION_CONFIG.FAVICON_PATH,

  // a lower cost way to genereate sourcemap, default is cheap-module-source-map, could save 60% time in dev hotload
  dva: {
    hmr: true,
    skipModelValidate: true,
  },
  history: {
    type: 'hash',
  },
  autoprefixer: {},

  layout: false,
  locale: {
    // default zh-CN
    default: 'zh-CN',
    antd: false,
    // default true, when it is true, will use `navigator.language` overwrite default
    baseNavigator: true,
  },
  dynamicImport: {
    loading: '@/components/PageLoading/index',
  },
  targets: { chrome: 49, firefox: 64, safari: 10, edge: 13, ios: 10 },
  // umi routes: https://umijs.org/docs/routing
  routes: routes,
  // Theme for antd: https://ant.design/docs/react/customize-theme-cn
  theme: {
    'primary-color': defaultSettings.primaryColor,
  },
  title: false,
  ignoreMomentLocale: true,
  // manifest: {
  //   basePath: '/',
  // },
  // exportStatic: {},
  esbuild: {},
  // mfsu: { },
  // chainWebpack: (memo, { env, webpack, createCSSRule }) => {
  //   memo.module
  //     .rule('compile')
  //     .test(/\.(svg)(\?.*)?$/)
  //     .use('file-loader')
  //     .loader('file-loader')
  //     .options({
  //       name: 'static/[name].[ext]',
  //       esModule: false,
  //     });
  // },
  // @see https://github.com/ant-design/ant-design-pro/issues/9308
  // 开启qiankun了以后，会渲染到root-master这个节点上，
  // 需要手动在umi的config里面配置mountElementId:"root"，或者修改document.ejs的id
  mountElementId: 'root-master',
});
