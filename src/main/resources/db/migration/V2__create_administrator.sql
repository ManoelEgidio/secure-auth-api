INSERT INTO users (id, created_at, name, login, password, role, enabled)
SELECT '20023ae1-f469-4753-9a95-0464e2c3ea3d', now(), 'Admin', 'administrator@adm.com',
       '$2a$12$8Uw8sVKsmplncMO7zn5OkuMpkcZYpV2e0WIEiKus8toJveKLDhed.', 'ADMIN', true
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'administrator@adm.com');

INSERT INTO user_authorities (user_id, authority)
SELECT '20023ae1-f469-4753-9a95-0464e2c3ea3d', unnest(ARRAY['CREATE', 'EDIT', 'DISABLE', 'VIEW', 'SEARCH'])
    WHERE NOT EXISTS (
    SELECT 1 FROM user_authorities WHERE user_id = '20023ae1-f469-4753-9a95-0464e2c3ea3d'
);
