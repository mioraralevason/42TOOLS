\c postgres
DROP DATABASE "e42";
CREATE DATABASE "e42";
\c e42


CREATE TABLE Image (
    image_id SERIAL PRIMARY KEY,
    link VARCHAR(250),
    large_ VARCHAR(250),
    medium VARCHAR(250),
    small VARCHAR(250),
    micro VARCHAR(250),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE User_ (
    user_id VARCHAR(250),
    email VARCHAR(250),
    login VARCHAR(250),
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    usual_full_name VARCHAR(250),
    usual_first_name VARCHAR(250),
    url VARCHAR(250),
    phone VARCHAR(250),
    displayname VARCHAR(250),
    kind VARCHAR(250),
    staff BOOLEAN,
    correction_point NUMERIC(15,2),
    anonymize_date TIMESTAMPTZ,
    data_erasure_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    alumnized_at TIMESTAMPTZ,
    alumni BOOLEAN,
    active BOOLEAN,
    pool_month VARCHAR(250),
    wallet NUMERIC(15,2),
    pool_year VARCHAR(50),
    location VARCHAR(250),
    image_id INTEGER NOT NULL,
    PRIMARY KEY(user_id),
    FOREIGN KEY(image_id) REFERENCES Image(image_id)
);

CREATE TABLE Stats (
    id SERIAL PRIMARY KEY,
    date_ DATE,
    duration INTERVAL,
    user_id VARCHAR(250),
    FOREIGN KEY(user_id) REFERENCES User_(user_id) ON DELETE CASCADE
);

CREATE TABLE Locations(
   locations_id VARCHAR(50) ,
   begin_at TIMESTAMPZ,
   end_at TIMESTAMPZ,
   primary_location BOOLEAN,
   floor_ VARCHAR(250) ,
   row_ VARCHAR(250) ,
   post VARCHAR(250) ,
   host VARCHAR(250) ,
   campus_id VARCHAR(50) ,
   created_at TIMESTAMPZ,
   updated_at TIMESTAMPZ,
   user_id VARCHAR(250)  NOT NULL,
   PRIMARY KEY(locations_id),
   FOREIGN KEY(user_id) REFERENCES User_(user_id)
);

ALTER TABLE Stats
ADD CONSTRAINT stats_user_date_unique UNIQUE (user_id, date_);
