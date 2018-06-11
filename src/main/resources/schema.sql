create table bank_transaction
(
   id BIGINT not null,
   amount DOUBLE not null,
   create_date_in_milis BIGINT not null,
   primary key(id)
);