create
database edge_computing;
use
edge_computing;

CREATE TABLE `ec_test_task`
(
    `id`                        bigint NOT NULL AUTO_INCREMENT,
    `source`                    varchar(20) DEFAULT NULL,
    `destination`               varchar(20) DEFAULT NULL,
    `status`                    varchar(20) DEFAULT NULL,
    `task_size`                 bigint      DEFAULT NULL,
    `task_complexity`           bigint      DEFAULT NULL,
    `cpu_cycle`                 bigint      DEFAULT NULL,
    `deadline`                  bigint      DEFAULT NULL,
    `transmission_waiting_time` bigint      DEFAULT NULL,
    `transmission_time`         bigint      DEFAULT NULL,
    `execution_waiting_time`    bigint      DEFAULT NULL,
    `execution_time`            bigint      DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `ec_edge_node`
(
    `id`                     int NOT NULL AUTO_INCREMENT,
    `cpu_num`                int         DEFAULT NULL,
    `execution_failure_rate` double      DEFAULT NULL,
    `task_rate`              double      DEFAULT NULL,
    `name`                   varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `ec_link`
(
    `id`                        int         NOT NULL AUTO_INCREMENT,
    `source`                    varchar(20) NOT NULL,
    `destination`               varchar(20) NOT NULL,
    `transmission_rate`         double      NOT NULL,
    `transmission_failure_rate` double      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;