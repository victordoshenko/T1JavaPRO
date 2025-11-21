INSERT INTO products (account_number, balance, product_type, user_id)
SELECT 'ACC-10001', 1200.50, 'ACCOUNT', id
FROM users
WHERE username = 'test_user_1'
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO products (account_number, balance, product_type, user_id)
SELECT 'CARD-20001', 500.00, 'CARD', id
FROM users
WHERE username = 'test_user_1'
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO products (account_number, balance, product_type, user_id)
SELECT 'ACC-10002', 3000.00, 'ACCOUNT', id
FROM users
WHERE username = 'test_user_2'
ON CONFLICT (account_number) DO NOTHING;


