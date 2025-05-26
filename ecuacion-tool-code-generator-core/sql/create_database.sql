--↓↓↓　postgresユーザで設定　↓↓↓--

--DB作成 ( Win)
--create database aws_instance_manager;
--★☆★☆　mac / linuxの場合は以下のようにしないと文字化ける　★☆★☆
create database code_generator with encoding 'utf8' template template0;

--接続するDBの変更
\c code_generator


--ユーザ作成
create user code_generator with password 'c8rFafqk' nocreatedb;

--schema作成
CREATE SCHEMA code_generator;

--権限設定
ALTER SCHEMA code_generator OWNER TO code_generator;

--（データ作成ではないが、よく忘れるので）スキーマの切り替え方法
select current_schema();
SET search_path = code_generator;
select current_schema();
