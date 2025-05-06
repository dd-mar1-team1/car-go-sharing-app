DELETE FROM rentals;
DELETE FROM cars;
DELETE FROM users;

INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (2, 'user@example.com', 'password', 'First', 'Last', 'CUSTOMER');

INSERT INTO cars (id, model, brand, type, inventory, daily_fee)
VALUES (1, 'Model S', 'Tesla', 'SEDAN', 5, 100.0);