-- Schema only. No personal data or account credentials.
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `dish_info`  (
  `dishId` int NOT NULL AUTO_INCREMENT,
  `dishName` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `chefName` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `dishPrice` decimal(10, 2) NOT NULL,
  `dishTypeId` int NOT NULL,
  `dishDesc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '书籍描述',
  `isReserved` tinyint NOT NULL COMMENT '1表示借出，0表示已还',
  `dishImg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '书籍图片',
  `kitchenName` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '供应档口/厨房',
  `stockQty` int NOT NULL DEFAULT 0 COMMENT '可售库存份数',
  `isAvailable` tinyint NOT NULL DEFAULT 1 COMMENT '是否上架 1上架 0下架',
  `prepMinutes` int NULL DEFAULT NULL COMMENT '预计制作时间(分钟)',
  `spiceLevel` tinyint NULL DEFAULT NULL COMMENT '辣度 0-5',
  `allergens` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '过敏原',
  `caloriesKcal` decimal(10, 2) NULL DEFAULT NULL COMMENT '热量(kcal)',
  `proteinG` decimal(10, 2) NULL DEFAULT NULL COMMENT '蛋白质(g)',
  `fatG` decimal(10, 2) NULL DEFAULT NULL COMMENT '脂肪(g)',
  `carbG` decimal(10, 2) NULL DEFAULT NULL COMMENT '碳水(g)',
  `createdAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`dishId`) USING BTREE,
  INDEX `fk_book_info_book_type_1`(`dishTypeId`) USING BTREE,
  INDEX `idx_dish_type_available`(`dishTypeId`, `isAvailable`) USING BTREE,
  INDEX `idx_dish_name`(`dishName`) USING BTREE,
  CONSTRAINT `dish_info_ibfk_1` FOREIGN KEY (`dishTypeId`) REFERENCES `dish_type` (`dishTypeId`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `dish_type`  (
  `dishTypeId` int NOT NULL AUTO_INCREMENT,
  `dishTypeName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `dishTypeDesc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '书籍类型描述',
  PRIMARY KEY (`dishTypeId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `meal_order`  (
  `orderId` int NOT NULL AUTO_INCREMENT,
  `residentId` int NOT NULL,
  `dishId` int NOT NULL,
  `orderTime` datetime NOT NULL,
  `completeTime` datetime NULL DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT 1 COMMENT '订餐份数',
  `unitPrice` decimal(10, 2) NULL DEFAULT NULL COMMENT '下单时单价',
  `totalPrice` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总价',
  `orderStatus` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
  `mealDate` date NULL DEFAULT NULL COMMENT '就餐日期',
  `mealSlot` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '餐次 BREAKFAST/LUNCH/DINNER',
  `deliveryType` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'PICKUP' COMMENT '取餐方式 PICKUP/DELIVERY',
  `pickupCode` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '取餐码',
  `contactPhone` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `deliveryAddress` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '配送地址',
  `remark` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `payStatus` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
  `paidAt` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `cancelReason` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '取消原因',
  `createdAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`orderId`) USING BTREE,
  INDEX `fk_borrow_user_1`(`residentId`) USING BTREE,
  INDEX `fk_borrow_book_info_1`(`dishId`) USING BTREE,
  INDEX `idx_order_resident_status`(`residentId`, `orderStatus`) USING BTREE,
  INDEX `idx_order_date_slot`(`mealDate`, `mealSlot`) USING BTREE,
  INDEX `idx_order_dish`(`dishId`) USING BTREE,
  CONSTRAINT `meal_order_ibfk_1` FOREIGN KEY (`dishId`) REFERENCES `dish_info` (`dishId`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `meal_order_ibfk_2` FOREIGN KEY (`residentId`) REFERENCES `resident` (`residentId`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `resident`  (
  `residentId` int NOT NULL AUTO_INCREMENT,
  `residentName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `residentPassword` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `roleType` tinyint NOT NULL COMMENT '1是管理员，0非管理员',
  `phone` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `gender` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `buildingNo` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '楼栋',
  `unitNo` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '单元',
  `roomNo` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '房号',
  `dietaryTags` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '饮食偏好标签',
  `healthNotes` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '健康提示/禁忌',
  `isActive` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态 1启用 0停用',
  `createdAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`residentId`) USING BTREE,
  INDEX `idx_resident_name`(`residentName`) USING BTREE,
  INDEX `idx_resident_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
