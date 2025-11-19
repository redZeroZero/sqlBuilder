DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS order_lines CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS jobs CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

CREATE TABLE departments (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    location TEXT NOT NULL,
    budget NUMERIC(12,2) NOT NULL
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    status TEXT NOT NULL,
    department_id INTEGER REFERENCES departments(id),
    manager_id INTEGER REFERENCES employees(id),
    hire_date DATE NOT NULL,
    salary NUMERIC(10,2) NOT NULL,
    bonus NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE jobs (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    salary NUMERIC(10,2) NOT NULL,
    employee_id INTEGER REFERENCES employees(id),
    start_date DATE NOT NULL,
    end_date DATE,
    job_type TEXT NOT NULL
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    sku TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    inventory_count INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL
);

CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    loyalty_level TEXT NOT NULL DEFAULT 'Bronze',
    city TEXT,
    country TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id),
    order_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status TEXT NOT NULL,
    shipping_method TEXT NOT NULL,
    taxes NUMERIC(12,2) NOT NULL DEFAULT 0,
    total NUMERIC(12,2) NOT NULL
);

CREATE TABLE order_lines (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    discount NUMERIC(10,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    amount NUMERIC(12,2) NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT NOW(),
    method TEXT NOT NULL,
    reference TEXT
);

INSERT INTO departments (name, location, budget) VALUES
('Engineering','Montreal',250000),
('Finance','Paris',180000),
('Marketing','Lyon',150000),
('Customer Success','Quebec City',120000);

INSERT INTO employees (first_name, last_name, email, phone, status, department_id, manager_id, hire_date, salary, bonus) VALUES
('Alice','Martin','alice.martin@corp.example','555-1001','ACTIVE',1,NULL,'2015-03-01',95000,15000),
('Bob','Dubois','bob.dubois@corp.example','555-1002','ACTIVE',1,1,'2017-08-15',88000,12000),
('Clara','Leroy','clara.leroy@corp.example','555-2001','ACTIVE',2,2,'2018-01-10',78000,8000),
('David','Marchand','david.marchand@corp.example','555-3001','ACTIVE',3,2,'2019-07-23',67000,5000),
('Eve','Tremblay','eve.tremblay@corp.example','555-1003','ON_LEAVE',1,2,'2020-04-18',72000,4000),
('Felix','Roy','felix.roy@corp.example','555-4001','ACTIVE',4,4,'2021-02-11',62000,3000);

INSERT INTO jobs (title, salary, employee_id, start_date, end_date, job_type) VALUES
('Senior Engineer',120000,1,'2019-01-01',NULL,'FULL_TIME'),
('Lead Engineer',110000,2,'2020-06-01',NULL,'FULL_TIME'),
('Financial Analyst',90000,3,'2021-03-15',NULL,'FULL_TIME'),
('Marketing Coordinator',65000,4,'2022-07-01',NULL,'FULL_TIME'),
('Consultant',75000,5,'2023-01-05','2024-01-05','CONTRACT'),
('Support Specialist',58000,6,'2022-09-10',NULL,'FULL_TIME');

INSERT INTO products (sku, name, category, description, inventory_count, price) VALUES
('W-PRO-001','Widget Pro','Hardware','High-end widget for demanding workloads',120,199.99),
('W-LITE-002','Widget Lite','Hardware','Entry-level widget with essential features',250,99.99),
('SRV-PLAN','Service Plan','Services','Extended service coverage for any widget',75,299.00),
('CONSULT-01','Consulting','Services','Professional consulting block priced per day',40,500.00),
('TRAIN-01','Training Workshop','Services','On-site training delivered by experts',20,890.00);

INSERT INTO customers (first_name, last_name, email, phone, loyalty_level, city, country) VALUES
('Émilie','Bernard','emilie.bernard@example.com','555-5001','Gold','Montreal','Canada'),
('Thierry','Rousseau','thierry.rousseau@example.com','555-5002','Silver','Quebec City','Canada'),
('Luc','Germain','luc.germain@example.com','555-5003','Bronze','Paris','France'),
('Sonia','Lapointe','sonia.lapointe@example.com','555-5004','Gold','Toronto','Canada');

INSERT INTO orders (customer_id, order_date, status, shipping_method, taxes, total) VALUES
(1, '2023-11-02', 'SHIPPED', 'Ground', 59.97, 559.94),
(2, '2023-11-12', 'PROCESSING', 'Courier', 29.90, 328.90),
(3, '2023-11-20', 'SHIPPED', 'Ground', 69.99, 744.97),
(4, '2023-12-01', 'PENDING', 'Express', 89.00, 979.00);

INSERT INTO order_lines (order_id, product_id, quantity, unit_price, discount, tax_amount) VALUES
(1, 1, 2, 199.99, 0.00, 49.98),
(1, 2, 1, 99.99, 0.00, 9.99),
(2, 3, 1, 299.00, 0.00, 29.90),
(3, 4, 1, 500.00, 25.00, 50.00),
(3, 2, 2, 99.99, 0.00, 19.99),
(4, 5, 1, 890.00, 0.00, 89.00);

INSERT INTO payments (order_id, amount, method, reference) VALUES
(1, 559.94, 'CARD', 'AUTH-10001'),
(2, 328.90, 'CARD', 'AUTH-10002'),
(3, 744.97, 'WIRE', 'WIRE-20001'),
(4, 489.50, 'CARD', 'AUTH-10003');
