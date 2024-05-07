create table if not exists USERS
(
    USER_ID          BIGINT auto_increment,
    USER_NAME        CHARACTER VARYING(64) not null,
    USER_EMAIL       CHARACTER VARYING(100) not null,
    constraint USERS_USER_ID_PK
        primary key (USER_ID),
    constraint UNIQUE_EMAIL unique (USER_EMAIL)
);

create table if not exists ITEMS
(
    ITEM_ID          BIGINT auto_increment,
    ITEM_NAME        CHARACTER VARYING(64) not null,
    ITEM_DESCRIPTION CHARACTER VARYING(1000) not null,
    ITEM_AVAILABLE   BOOLEAN   not null,
    ITEM_OWNER_ID    BIGINT    not null,
    constraint ITEMS_ITEM_ID_PK
        primary key (ITEM_ID),
    constraint ITEMS_USERS_USER_ID_FK
        foreign key (ITEM_OWNER_ID) references USERS on delete cascade
);
