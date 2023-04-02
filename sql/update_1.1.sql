alter table t_user drop refresh_token;
alter table t_user drop token_host;
alter table t_user add token VARCHAR(100) NOT NULL DEFAULT '';

alter table t_user drop approved;
alter table t_user drop approval_code;
alter table t_user drop email_verified;
alter table t_user drop failed_login_count;