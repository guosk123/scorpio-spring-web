升级脚本用于升级自创世版本依赖的数据结构（schema）和数据（data）的升级
每个发布版本提供0~N个升级脚本，根据数据类型的不同使用不同的文件后缀，例如：.sql（用于postgresql升级）、.js（用于Mongo升级）（ES升级待定）
每个升级脚本的命名方式遵循：update_数据库类型（postgresql/mongo/elastic）_升级前版本号_升级后版本号.后缀