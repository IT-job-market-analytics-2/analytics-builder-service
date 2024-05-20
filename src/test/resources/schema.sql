create table if not exists Vacancy_analytics (
    id bigint auto_increment primary key,
    date date,
    query varchar(255),
    vacancy_count bigint,
    average_salary decimal(10,2)
    );