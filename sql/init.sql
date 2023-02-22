-- noinspection SqlNoDataSourceInspectionForFile

CREATE SEQUENCE IF NOT EXISTS s_company_id START 1000;

CREATE TABLE IF NOT EXISTS t_company
(
    id            INTEGER       NOT NULL,
    creation_date TIMESTAMP     NOT NULL DEFAULT now(),
    change_date   TIMESTAMP     NOT NULL DEFAULT now(),
    name          VARCHAR(255)  NOT NULL,
    street        VARCHAR(255)  NOT NULL,
    zipCode       VARCHAR(20)   NOT NULL,
    city          VARCHAR(255)  NOT NULL,
    country       VARCHAR(255)  NOT NULL DEFAULT '',
    email         VARCHAR(255)  NOT NULL,
    phone         VARCHAR(50)   NOT NULL DEFAULT '',
    fax           VARCHAR(50)   NOT NULL DEFAULT '',
    description   VARCHAR(2000) NOT NULL DEFAULT '',
    CONSTRAINT t_company_pk PRIMARY KEY (id)
);

CREATE SEQUENCE s_group_id START 1000;
CREATE TABLE IF NOT EXISTS t_group
(
    id          INTEGER      NOT NULL,
    change_date TIMESTAMP    NOT NULL DEFAULT now(),
    name        VARCHAR(100) NOT NULL,
    notes       VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT t_group_pk PRIMARY KEY (id)
);

CREATE SEQUENCE s_user_id START 1000;
CREATE TABLE IF NOT EXISTS t_user
(
    id                 INTEGER      NOT NULL,
    change_date        TIMESTAMP    NOT NULL DEFAULT now(),
    company_id         INTEGER      NULL,
    title              VARCHAR(30)  NOT NULL DEFAULT '',
    first_name         VARCHAR(100) NOT NULL DEFAULT '',
    last_name          VARCHAR(100) NOT NULL,
    street             VARCHAR(100) NOT NULL DEFAULT '',
    zipCode            VARCHAR(16)  NOT NULL DEFAULT '',
    city               VARCHAR(50)  NOT NULL DEFAULT '',
    country            VARCHAR(50)  NOT NULL DEFAULT '',
    email              VARCHAR(100) NOT NULL DEFAULT '',
    phone              VARCHAR(50)  NOT NULL DEFAULT '',
    fax                VARCHAR(50)  NOT NULL DEFAULT '',
    mobile             VARCHAR(50)  NOT NULL DEFAULT '',
    notes              VARCHAR(500) NOT NULL DEFAULT '',
    portrait_name      VARCHAR(255) NOT NULL DEFAULT '',
    portrait           BYTEA        NULL,
    login              VARCHAR(30)  NOT NULL,
    pwd                VARCHAR(100) NOT NULL,
    refresh_token      VARCHAR(2000) NOT NULL DEFAULT '',
    token_host         VARCHAR(500) NOT NULL DEFAULT '',
    approval_code      VARCHAR(50)  NOT NULL DEFAULT '',
    approved           BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_count INTEGER      NOT NULL DEFAULT 0,
    locked             BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT t_user_pk PRIMARY KEY (id),
    CONSTRAINT t_user_fk1 FOREIGN KEY (company_id) REFERENCES t_company (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS t_user2group
(
    user_id  INTEGER NOT NULL,
    group_id INTEGER NOT NULL,
    CONSTRAINT t_user2group_pk PRIMARY KEY (user_id,
                                            group_id),
    CONSTRAINT t_user2group_fk1 FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT t_user2group_fk2 FOREIGN KEY (group_id) REFERENCES t_group (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_system_right
(
    name     VARCHAR(30) NOT NULL,
    group_id INTEGER     NOT NULL,
    CONSTRAINT t_system_right_pk PRIMARY KEY (name,
                                              group_id),
    CONSTRAINT t_system_fk1 FOREIGN KEY (group_id) REFERENCES t_group (id) ON DELETE CASCADE
);

CREATE TABLE t_timer_task
(
    name               VARCHAR(60)  NOT NULL,
    display_name       VARCHAR(255) NOT NULL,
    execution_interval VARCHAR(30)  NOT NULL,
    day                INTEGER      NOT NULL DEFAULT 0,
    hour               INTEGER      NOT NULL DEFAULT 0,
    minute             INTEGER      NOT NULL DEFAULT 0,
    last_execution     TIMESTAMP    NULL,
    note_execution     BOOLEAN      NOT NULL DEFAULT FALSE,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT t_timer_task_pk PRIMARY KEY (name)
);

-- root user
INSERT INTO t_user (id,first_name,last_name,email,login,pwd,approval_code,approved)
VALUES (1,'System','Administrator','root@localhost','root','','',TRUE);

-- timer
INSERT INTO t_timer_task (name,display_name,execution_interval,minute,active)
VALUES ('heartbeat','Heartbeat Task','CONTINOUS',5,FALSE);
INSERT INTO t_timer_task (name,display_name,execution_interval,minute,active)
VALUES ('cleanup','Cleanup Task','CONTINOUS',5,FALSE);

--- set pwd 'pass' dependent on salt V3xfgDrxdl8=
-- root user
update t_user set pwd='A0y3+ZmqpMhWA21VFQMkyY6v74Y=' where id=1;
