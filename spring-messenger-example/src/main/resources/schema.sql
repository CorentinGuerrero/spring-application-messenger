create table if not exists app_users (
  id uuid primary key,
  email varchar(320) not null unique,
  display_name varchar(200) not null,
  created_at timestamp not null
);
