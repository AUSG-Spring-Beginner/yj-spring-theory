create user 'spring5'@'localhost' identified by 'spring5';

create database spring5fs character set=utf8;

grant all privileges on spring5fs.* to 'spring5'@'localhost';

use spring5fs;

create table MEMBER (
    ID int auto_increment primary key,
    EMAIL varchar(255),
    PASSWORD varchar(100),
    NAME varchar(100),
    REGDATE datetime,
    unique key (EMAIL)
) engine=InnoDB character set = utf8;

insert into spring5fs.MEMBER(EMAIL, PASSWORD, NAME) values ('ddd@gmail.com', 'abcd1234', 'dada');

select * from member;