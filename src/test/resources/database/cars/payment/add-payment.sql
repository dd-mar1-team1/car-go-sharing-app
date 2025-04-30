DELETE FROM payments;
DELETE FROM rentals;
DELETE FROM cars;
DELETE FROM users;

INSERT INTO users (id, email, password, first_name, last_name, role)
VALUES (1, 'user@example.com', 'password', 'Test', 'User', 'MANAGER');

INSERT INTO cars (id, model, brand, type, inventory, daily_fee)
VALUES (1, 'Model S', 'Tesla', 'SEDAN', 5, 150.0);

INSERT INTO rentals (id, rental_date, return_date, car_id, user_id)
VALUES (1, '2025-04-01', '2025-04-10', 1, 1);

INSERT INTO payments (id, status, type, rental_id, session_url, session_id, amount_to_pay, is_deleted)
VALUES (1, 'PAID', 'PAYMENT', 1, 'https://fake-url.com/session', 'sess_123456', 500.00, false);