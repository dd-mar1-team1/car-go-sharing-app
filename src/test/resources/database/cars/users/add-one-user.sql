DELETE FROM users WHERE id = 1;

INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (1, 'user@example.com', '$2a$12$PmPcRZXBnewKtxelNyen4.Q8HZPAZ9qamXBMOq.eQ7gxZwrCBQ8/a', 'Test', 'User', 'MANAGER');
