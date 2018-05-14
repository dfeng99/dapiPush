-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- 主機: localhost
-- 產生時間： 2018 年 05 月 14 日 21:41
-- 伺服器版本: 5.7.22-0ubuntu0.16.04.1
-- PHP 版本： 7.0.28-0ubuntu0.16.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫： `DapiPush`
--
CREATE DATABASE IF NOT EXISTS `DapiPush` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `DapiPush`;

-- --------------------------------------------------------

--
-- 資料表結構 `notification_push_blacklist`
--

DROP TABLE IF EXISTS `notification_push_blacklist`;
CREATE TABLE `notification_push_blacklist` (
  `provider` set('apns','fcm') COLLATE utf8mb4_unicode_ci NOT NULL,
  `to_token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `state` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'sent_request'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- 表的關聯 `notification_push_blacklist`:
--

-- --------------------------------------------------------

--
-- 資料表結構 `Statistics`
--

DROP TABLE IF EXISTS `Statistics`;
CREATE TABLE `Statistics` (
  `user_id` int(11) NOT NULL,
  `provider` set('apns','fcm') COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_access` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `counter` bigint(20) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- 表的關聯 `Statistics`:
--

-- --------------------------------------------------------

--
-- 資料表結構 `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- 表的關聯 `users`:
--

--
-- 已匯出資料表的索引
--

--
-- 資料表索引 `notification_push_blacklist`
--
ALTER TABLE `notification_push_blacklist`
  ADD PRIMARY KEY (`to_token`),
  ADD KEY `user_id` (`user_id`);

--
-- 資料表索引 `Statistics`
--
ALTER TABLE `Statistics`
  ADD PRIMARY KEY (`user_id`,`provider`);

--
-- 資料表索引 `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- 在匯出的資料表使用 AUTO_INCREMENT
--

--
-- 使用資料表 AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
