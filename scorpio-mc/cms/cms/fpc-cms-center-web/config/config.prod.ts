// https://umijs.org/config/
import moment from 'moment';
import { defineConfig } from 'umi';
import APPLICATION_CONFIG from '../src/common/applicationConfig';

console.log('prod env');

export default defineConfig({
  externals: {
    react: 'React',
    'react-dom': 'ReactDOM',
    jquery: 'jQuery',
    lodash: '_',
  },
  scripts: [
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/react-17.0.2.production.min.js` },
    {
      src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/react-dom-17.0.2.production.min.js`,
    },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/jquery-3.5.1.min.js` },
    // { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/moment-2.29.1.min.js` },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/lodash-4.17.20.min.js` },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/config/config.js` },
  ],
  metas: [
    { name: 'build-time', content: moment().format() },
    { name: 'build-version', content: '' },
  ],
  title: false,
  ignoreMomentLocale: true,
  // umi3 comple node_modules by default, could be disable
  // nodeModulesTransform: {
  //   type: 'none',
  //   exclude: [],
  // },
  // manifest: {
  //   basePath: '/',
  // },
  // exportStatic: {},
  esbuild: {},
  chunks: ['vendors', 'umi'],
  chainWebpack: function (config, { webpack }) {
    config.module.rule('mjs-rule').test(/.m?js/).resolve.set('fullySpecified', false);
    config.merge({
      optimization: {
        splitChunks: {
          chunks: 'all',
          minSize: 30000,
          minChunks: 3,
          automaticNameDelimiter: '.',
          cacheGroups: {
            vendor: {
              name: 'vendors',
              test({ resource }: { resource: string }) {
                return /[\\/]node_modules[\\/]/.test(resource);
              },
              priority: 10,
            },
          },
        },
      },
    });
  },
});
