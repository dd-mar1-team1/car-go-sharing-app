DELETE FROM rentals;
DELETE FROM cars;
DELETE FROM users;

INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (2, 'customer@example.com', 'password', 'First', 'Last', 'CUSTOMER');

INSERT INTO cars (id, model, brand, type, inventory, daily_fee)
VALUES (1, 'Model S', 'Tesla', 'SEDAN', 5, 150.0);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id)
VALUES (1, '2025-04-25', '2025-04-30', NULL, 1, 2);
