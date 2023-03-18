INSERT INTO roles(id, name)
VALUES (1, 'ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles(id, name)
VALUES (2, 'ROLE_ADMIN') ON CONFLICT DO NOTHING;

/*
INSERT INTO users(id, email, password, username)
VALUES (0, 'admin@admin.lu', '$2a$10$vnbLuJrMxF6brARBGTSo.eUaTc1My.Rf.2bjnsFIcbXEwm1oztF3.',
        'admin') ON CONFLICT DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
VALUES (0, 1) ON CONFLICT DO NOTHING;

INSERT INTO user_roles(user_id, role_id)
VALUES (0, 2) ON CONFLICT DO NOTHING;
*/
