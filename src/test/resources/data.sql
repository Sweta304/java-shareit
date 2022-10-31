--Для интеграционных тестов
insert into users (user_name, email)
values ('Sveta', 'mail@mail.ru'), ('User', 'mailq@mail.ru');

insert into requests (description, created, requestor_id)
values ('description', '2022-10-29 12:54:13', 1);

insert into items (item_name, description, available, owner_id, request_id)
values ('name', 'description', true, 1, 1);

insert into booking (booking_start, booking_end, item_id, booker_id, status)
values ('2022-11-03 12:54:13', '2022-11-27 12:54:13', 1, 1, 'APPROVED'),
       ('2022-12-03 12:54:13', '2022-12-27 12:54:13', 1, 1, 'APPROVED');


