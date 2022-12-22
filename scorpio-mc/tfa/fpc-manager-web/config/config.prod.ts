// https://umijs.org/config/
import moment from 'moment';
import { defineConfig } from 'umi';
import APPLICATION_CONFIG from '../src/common/applicationConfig';
import MonacoWebpackPlugin from 'monaco-editor-webpack-plugin';
// import { execSync } from 'child_process';

// const gitCommit = execSync('git rev-parse --short HEAD').toString().trim();
// const gitBranch = execSync('git symbolic-ref --short HEAD').toString().trim();

// console.log(gitBranch, gitCommit);

export default defineConfig({
  externals: {
    react: 'React',
    'react-dom': 'ReactDOM',
    jquery: 'jQuery',
    lodash: '_',
    '@antv/x6': 'X6',
  },
  scripts: [
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/react.production-17.0.2.min.js` },
    {
      src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/react-dom.production-17.0.2.min.js`,
    },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/jquery-3.6.0.min.js` },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/lodash-4.17.20.min.js` },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/libs/antv-x6-1.28.1.min.js` },
    { src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/config/config.js` },
  ],
  metas: [
    { name: 'build-time', content: moment().format() },
    { name: 'build-version', content: '' },
    // { name: 'git-branch', content: gitBranch },
    // { name: 'git-commit', content: gitCommit }
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
    // config.module.rule('mjs-rule').test(/.m?js/).resolve.set('fullySpecified', false);
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
    config.plugin('monaco-editor').use(MonacoWebpackPlugin, [
      {
        languages: ['lua'],
      },
    ]);
  },
});
