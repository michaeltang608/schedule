/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 50727
 Source Host           : 127.0.0.1
 Source Database       : schedule

 Target Server Type    : MySQL
 Target Server Version : 50727
 File Encoding         : utf-8

 Date: 06/18/2022 04:45:19 AM
*/
CREATE database if NOT EXISTS `schedule` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `schedule`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `task_info`
-- ----------------------------
DROP TABLE IF EXISTS `task_info`;
CREATE TABLE `task_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) NOT NULL COMMENT 'appName',
  `task_name` varchar(12) NOT NULL COMMENT 'taskName',
  `cron` varchar(20) NOT NULL COMMENT 'cron表达式',
  `trigger_last_time` datetime DEFAULT NULL,
  `trigger_next_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_taskName` (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `trigger_log`
-- ----------------------------
DROP TABLE IF EXISTS `trigger_log`;
CREATE TABLE `trigger_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` int(11) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
