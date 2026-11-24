-- ============================================================
-- Sistema de Gestão de Horários de Professores
-- Script de criação do banco de dados e do usuário da aplicação.
--
-- Execute como usuário administrador do MySQL:
--   mysql -u root -p < database/criar-banco.sql
--
-- As tabelas são criadas/atualizadas automaticamente pelo Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) na primeira execução da API.
-- ============================================================

CREATE DATABASE IF NOT EXISTS horarios_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'horarios'@'localhost' IDENTIFIED BY 'horarios123';

GRANT ALL PRIVILEGES ON horarios_db.* TO 'horarios'@'localhost';

FLUSH PRIVILEGES;
